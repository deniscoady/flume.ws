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

import com.deniscoady.flume.websocket.security.SSLSocketFactoryBuilder;
import com.deniscoady.flume.websocket.util.Delay;
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
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

public class WebSocketSource extends AbstractSource implements Configurable, EventDrivenSource {

    /**
     * Configuration object holding Flume properties.
     */
    private Configuration configuration = null;

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
     * Parse Flume context for configuration properties and setup the websocket client factory.
     *
     * @param context Flume context
     */
    @Override
    public void configure(Context context) {
        configuration = new Configuration(context);
        try {
            webSocket = new WebSocketClient.Builder()
                .setEndpoint(configuration.getEndpointAddress())
                .onOpen(this::onOpen)
                .onMessage(this::onMessage)
                .onClose(this::onClose)
                .onError(ex -> logger.error(ex));

            if (configuration.isSecure()) {
                logger.info("SSL Enabled, setting up SSLSocketFactory");
                webSocket.setSocketFactory(new SSLSocketFactoryBuilder(
                    configuration.getKeyStoreType(),
                    configuration.getKeyStorePath(),
                    configuration.getKeyStorePassword(),
                    configuration.trustAllCertificates()
                ).getSSLSocketFactory());
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
    }

    /**
     * Called by Flume to stop this source.
     */
    @Override
    public void stop() {
        closeConnection();
        sourceCounter.stop();
        super.stop();
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
     * @param serverHandshake The websocket server http status response
     */
    private void onOpen(ServerHandshake serverHandshake) {
        logger.info("onOpen()");
        if (isConnected() && configuration.hasInitializationMessage()) {
            String message = configuration.getInitializationMessage();
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
        if (isRunning()) {
            Delay.awaitCondition(configuration.getRetryDelay() * 1000, this::isRunning);
            openConnection();
        }
    }

    /**
     * Asynchronous event handler triggered when the websocket client receives a message from the remote endpoint.
     * Creates a new Flume event and forwards to the connected Channels.
     *
     * @param message A string buffer containing a message sent from the remote endpoint
     */
    private void onMessage(String message) {
        logger.info("Message(" + message + ")");
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
