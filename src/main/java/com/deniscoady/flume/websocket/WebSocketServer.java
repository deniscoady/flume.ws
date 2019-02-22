package com.deniscoady.flume.websocket;

import com.deniscoady.flume.websocket.util.DefaultWebSocketServerConsumer;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultWebSocketServerFactory;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
    /**
     * SLF4j logger for debugging.
     */
    private Logger logger = Logger.getLogger(WebSocketServer.class);

    /**
     * Asynchronous event handler triggered on a client error.
     */
    private BiConsumer<WebSocket, Exception> errorConsumer;

    /**
     * Constructor of a new websocket client
     *
     * @param address Endpoint address to listen on
     * @param errorConsumer event handler for client errors
     */
    private WebSocketServer(
            InetSocketAddress address,
            BiConsumer<WebSocket, Exception> errorConsumer) {
        super(address);
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.debug("Client joined: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        logger.debug("Client left: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        webSocket.send(s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        errorConsumer.accept(webSocket, e);
    }

    @Override
    public void onStart() {
        logger.info("Started websocket server on " + this.getAddress());
    }



    /**
     * Helps create new clients in a factory pattern.
     */
    public static class Builder {
        /**
         * Client under construction by the builder
         */
        private WebSocketServer server;

        /**
         * Endpoint address for client to establish connection
         */
        private InetSocketAddress address;

        /**
         * Asynchronous event handler triggered on a client error.
         */
        private BiConsumer<WebSocket, Exception> errorConsumer = DefaultWebSocketServerConsumer::onError;

        /**
         * Socket factory to be used by websockets. Important to allow for custom socket factories when dealing with
         * TLS encryption.
         */
        private WebSocketServerFactory socketFactory = new DefaultWebSocketServerFactory();

        /**
         * Set remote endpoint URI
         *
         * @param address address to listen on
         * @return this Builder object
         */
        public Builder setBindAddress(InetSocketAddress address) {
            this.address = address;
            return this;
        }

        /**
         *
         * @param consumer event handler for connection error events
         * @return this Builder object
         */
        public Builder onError(BiConsumer<WebSocket, Exception> consumer) {
            this.errorConsumer = consumer;
            return this;
        }

        /**
         * Socket factory used to create websockets. Exposed for use in TLS encryption which require more sophisticated
         * socket factories.
         *
         * @param socketFactory Socket factory used to create websockets
         * @return this Builder object
         */
        public Builder setSocketFactory(WebSocketServerFactory socketFactory) {
            this.socketFactory = socketFactory;
            return this;
        }

        /**
         * Return built and listening websocket server
         *
         * @return connected websocket client
         */
        public WebSocketServer listen() {
            WebSocketServer instance = new WebSocketServer(this.address, this.errorConsumer);
            instance.setWebSocketFactory(this.socketFactory);
            instance.start();
            return instance;
        }
    }
}
