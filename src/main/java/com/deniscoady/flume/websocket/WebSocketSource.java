/*
 * Copyright 2018 Denis Coady
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.deniscoady.flume.websocket;

import com.deniscoady.flume.websocket.security.SSLBuilder;
import com.deniscoady.flume.websocket.util.TimerTaskAdapter;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.source.AbstractSource;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

public class WebSocketSource extends AbstractSource implements Configurable, EventDrivenSource {

    /**
     * SourceConfiguration object holding Flume properties.
     */
    private SourceConfiguration sourceConfiguration = null;

    /**
     * SLF4j logger for debugging.
     */
    private Logger logger = Logger.getLogger(WebSocketSource.class);

    /**
     * Source counter to report inputs and outputs over JMX by Apache Flume. Helps to identify pipeline issues.
     */
    private SourceCounter sourceCounter = new SourceCounter(getName());

    /**
     * Websocket client builder being used in a factory pattern.
     */
    private WebSocketClient.Builder webSocket = null;

    /**
     * Active websocket connection.
     */
    private WebSocketClient connection = null;

    /**
     * Timer used to automatically reconnect on unexpected server socket closure.
     */
    private Timer timer = new Timer();

    /**
     * Parse Flume context for SourceConfiguration properties and setup the websocket client factory.
     *
     * @param context Flume context
     */
    @Override
    public void configure(Context context) {
        sourceConfiguration = new SourceConfiguration(context);
        try {
            webSocket = new WebSocketClient.Builder()
                .setEndpoint(sourceConfiguration.getEndpointAddress())
                .setHttpCookie(sourceConfiguration.getCookies())
                .onOpen(serverHandshake -> onOpen())
                .onMessage(this::onMessage)
                .onClose(this::onClose)
                .onError(ex -> logger.error(ex));

            logger.info("Cookies:");
            Map<String, String> cookies = sourceConfiguration.getCookies();
            for (String key : cookies.keySet()) {
                logger.info("  " + key + " = " + cookies.get(key));
            }

            if (sourceConfiguration.isSecure()) {
                logger.info("SSL Enabled, setting up SSLSocketFactory");
                webSocket.setSocketFactory(getSocketFactory());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by Flume to start this source.
     */
    @Override
    public void start() {
        sourceCounter.start();
        openConnection();
        super.start();
        timer.schedule(
                new TimerTaskAdapter(this::openConnection),
                sourceConfiguration.getRetryDelay() * 1000);
    }

    /**
     * Called by Flume to stop this source.
     */
    @Override
    public void stop() {
        timer.cancel();
        closeConnection();
        sourceCounter.stop();
        super.stop();
    }

    /**
     * Create SSLSocketFactory. If user presented a Java KeyStore then one is created with those trusted certificates.
     *
     * @return SSL socket factory
     */
    private SocketFactory getSocketFactory() {
        SocketFactory factory = SSLSocketFactory.getDefault();
        if (sourceConfiguration.getKeyStoreType() != null
        &&  sourceConfiguration.getKeyStorePath() != null) {
            factory = new SSLBuilder(
                    sourceConfiguration.getKeyStoreType(),
                    sourceConfiguration.getKeyStorePath(),
                    sourceConfiguration.getKeyStorePassword(),
                    sourceConfiguration.trustAllCertificates()
            ).getSSLSocketFactory();
        }
        return factory;
    }

    /**
     * Check to see if an active connection exists.
     *
     * @return true if connected
     */
    private boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    /**
     * Check to see if no active connection exists.
     *
     * @return true if not connected
     */
    private boolean isDisconnected() {
        return connection == null || connection.isClosed();
    }

    /**
     * Check to see if source is currently running
     *
     * @return true if source is not in a STOP or ERROR lifecycle state
     */
    private boolean isRunning() {
        LifecycleState state = getLifecycleState();
        return state == LifecycleState.IDLE || state == LifecycleState.START;
    }

    /**
     * Opens a new websocket connection if this source is currently running and does not already have an active
     * connection.
     */
    private void openConnection() {
        if (isRunning() && !isConnected()) {
            try {
                logger.info("Opening connection");
                connection = webSocket.connect();
                connection.setReuseAddr(true);
                connection.setTcpNoDelay(true);
                connection.setConnectionLostTimeout(10);
                sourceCounter.setOpenConnectionCount(1);
                logger.info("Connection opened");
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     * Closes an active websocket connection if one exists.
     */
    private void closeConnection() {
        if (!isDisconnected()) {
            logger.info("Closing connection");
            connection.close();
        }
        sourceCounter.setOpenConnectionCount(0);
        logger.info("Connection closed");
    }

    /**
     * Asynchronous event handler triggered when the websocket client successfully opens a connection. Sends the
     * initialization message if defined.
     *
     */
    private void onOpen() {
        logger.info("onOpen()");
        if (isConnected() && sourceConfiguration.hasInitializationMessage()) {
            String message = sourceConfiguration.getInitializationMessage();
            logger.info("Sending initialization message: \n" + message);
            connection.send(message);
        }
    }

    /**
     * Asynchronous event handler triggered when the websocket client successfully closes a connection. If this source
     * is still running then a new connection will be opened after the configured retry delay.
     *
     * @param closeCode interger status code defining why the connection was closed
     */
    private void onClose(Integer closeCode) {
        logger.info("Connection closed, code=" + closeCode);
    }

    /**
     * Asynchronous event handler triggered when the websocket client receives a message from the remote endpoint.
     * Creates a new Flume event and forwards to the connected Channels.
     *
     * @param message A string buffer containing a message sent from the remote endpoint
     */
    private void onMessage(String message) {
        logger.debug("Message(" + message + ")");
        sourceCounter.incrementEventReceivedCount();
        ChannelProcessor processor = getChannelProcessor();
        processor.processEvent(createEvent(message));
        sourceCounter.incrementEventAcceptedCount();
    }

    /**
     * Wraps the received message in a Flume SimpleEvent.
     *
     * @param message the received string message
     * @return a new SimpleEvent containing the message
     */
    private Event createEvent(String message) {
        Event event = new SimpleEvent();
        event.setHeaders(createEventHeaders());
        event.setBody(message.getBytes());
        return event;
    }

    /**
     * Creates a set of empty event headers.
     *
     * @return an empty Map of headers
     */
    private Map<String, String> createEventHeaders() {
        return new TreeMap<>();
    }
}
