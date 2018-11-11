package com.deniscoady.flume.websocket;

import com.deniscoady.flume.websocket.security.SSLBuilder;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class WebSocketSink extends AbstractSink implements Configurable, Sink {

    /**
     * SourceConfiguration object holding Flume properties.
     */
    private SinkConfiguration sinkConfiguration = null;

    /**
     * SLF4j logger for debugging.
     */
    private Logger logger = Logger.getLogger(WebSocketSink.class);

    /**
     * Websocket client builder being used in a factory pattern.
     */
    private WebSocketServer.Builder builder = null;
    private WebSocketServer server = null;

    private SinkCounter sinkCounter = new SinkCounter(getName());

    /**
     * Processes queued events upstream and broadcasts to all connected clients.
     *
     * @return status of processing
     * @throws EventDeliveryException
     */
    @Override
    public Status process() throws EventDeliveryException {
        sinkCounter.incrementEventDrainAttemptCount();
        Channel channel = getChannel();
        Transaction transaction = channel.getTransaction();
        Status status;
        logger.info("Processing.");
        if (server != null) {
            transaction.begin();
            Event event = channel.take();
            if (event != null) {
                String message = new String(event.getBody());
                logger.info(message);
                server.broadcast(message);
                sinkCounter.incrementEventDrainSuccessCount();
            }
            transaction.commit();
            transaction.close();
            status = Status.READY;
        } else {
            logger.error("Server not running. Cannot process events.");
            status = Status.BACKOFF;
        }
        return status;
    }

    /**
     * Parse Flume context for SinkConfiguration properties and setup the websocket server factory.
     *
     * @param context Flume context
     */
    @Override
    public void configure(Context context) {
        sinkConfiguration = new SinkConfiguration(context);
        try {
            builder = new WebSocketServer.Builder()
                    .setBindAddress(sinkConfiguration.getInetSocketAddress())
                    .onError(this::onError);

            if (sinkConfiguration.isSecure()) {
                logger.info("SSL Enabled, setting up SSLContext");
                builder.setSocketFactory(new DefaultSSLWebSocketServerFactory(getSSLContext()));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by Flume to start this source.
     */
    @Override
    public void start() {
        if (server == null) {
            sinkCounter.incrementConnectionCreatedCount();
            server = builder.listen();
        }
    }

    /**
     * Called by Flume to stop this source.
     */
    @Override
    public void stop() {
        try {
            server.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            sinkCounter.incrementConnectionClosedCount();
            server = null;
        }
    }

    /**
     *
     * @param webSocket websocket of client where exception generated
     * @param ex the exception generated
     */
    private void onError(WebSocket webSocket, Exception ex) {
        webSocket.close();
        logger.error(ex);
    }

    /**
     * Create SSLSocketFactory. If user presented a Java KeyStore then one is created with those trusted certificates.
     *
     * @return SSL socket factory
     */
    private SSLContext getSSLContext() throws NoSuchAlgorithmException {
        SSLContext context = SSLContext.getDefault();
        if (sinkConfiguration.getKeyStoreType() != null
        &&  sinkConfiguration.getKeyStorePath() != null) {

            logger.info("Key Store Type : " + sinkConfiguration.getKeyStoreType());
            logger.info("Key Store Path : " + sinkConfiguration.getKeyStorePath());
            logger.info("Key Store Pass : " + sinkConfiguration.getKeyStorePassword());
            logger.info("Trust All Certs: " + sinkConfiguration.trustAllCertificates());

            context = new SSLBuilder(
                    sinkConfiguration.getKeyStoreType(),
                    sinkConfiguration.getKeyStorePath(),
                    sinkConfiguration.getKeyStorePassword(),
                    sinkConfiguration.trustAllCertificates()
            ).getSSLContext();
        }
        return context;
    }
}
