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

import org.apache.log4j.Logger;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    /**
     * SLF4j logger for debugging.
     */
    private static Logger logger = Logger.getLogger(WebSocketClient.class);

    /**
     * HTTP headers passed on connection
     */
    private Map<String, String> httpHeaders;

    /**
     * Asynchronous event handler triggered on a successful opening of a connection.
     */
    private Consumer<ServerHandshake> openConsumer;

    /**
     * Asynchronous event handler triggered on a receipt of a message.
     */
    private Consumer<String> messageConsumer;

    /**
     * Asynchronous event handler triggered on a closing of the connection.
     */
    private Consumer<Integer> closeConsumer;

    /**
     * Asynchronous event handler triggered on a client error.
     */
    private Consumer<Exception> errorConsumer;

    /**
     * Constructor of a new websocket client
     *
     * @param serverUri Endpoint address for client to connect to
     * @param openConsumer event handler for opening a connection
     * @param messageConsumer event handler for receiving a message
     * @param closeConsumer event handler for closing a connection
     * @param errorConsumer event handler for client errors
     */
    private WebSocketClient(
        URI serverUri,
        Map<String ,String> httpHeaders,
        Consumer<ServerHandshake> openConsumer,
        Consumer<String> messageConsumer,
        Consumer<Integer> closeConsumer,
        Consumer<Exception> errorConsumer) {
        super(serverUri, httpHeaders);
        this.httpHeaders = httpHeaders;
        this.openConsumer = openConsumer;
        this.messageConsumer = messageConsumer;
        this.closeConsumer = closeConsumer;
        this.errorConsumer = errorConsumer;
    }

    /**
     * Asynchronous event handler triggered when the websocket client successfully opens a connection. Sends the
     * initialization message if defined.
     *
     * @param serverHandshake The websocket server http status response
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.openConsumer.accept(serverHandshake);
    }

    /**
     * Asynchronous event handler triggered when the websocket client receives a message from the remote endpoint.
     *
     * @param message A string buffer containing a message sent from the remote endpoint
     */
    @Override
    public void onMessage(String message) {
        this.messageConsumer.accept(message);
    }

    /**
     * Asynchronous event handler triggered when the websocket client closes a connection.
     *
     * @param code status code defining why the connection was closed
     * @param reason a string message explaining why the connection was closed
     * @param remote true if the remote server closed the connection
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("onClose: code=" + code + ", reason=" + reason + ", remote=" + remote);
        this.closeConsumer.accept(code);
    }

    /**
     * Asynchronous event handler triggered when the websocket client encounters an error.
     *
     * @param exception the thrown exception describing the error encountered
     */
    @Override
    public void onError(Exception exception) {
        this.errorConsumer.accept(exception);
    }

    public Map<String, String> getHttpHeaders() {
        return this.httpHeaders;
    }

    /**
     * Helps create new clients in a factory pattern.
     */
    public static class Builder {
        /**
         * Endpoint address for client to establish connection
         */
        private URI endpoint;

        /**
         * HTTP Headers
         */
        private Map<String, String> httpHeaders = new HashMap<>();

        /**
         * Asynchronous event handler triggered on a successful opening of a connection.
         */
        private Consumer<ServerHandshake> openConsumer;

        /**
         * Asynchronous event handler triggered on a receipt of a message.
         */
        private Consumer<String> messageConsumer;

        /**
         * Asynchronous event handler triggered on a closing of the connection.
         */
        private Consumer<Integer> closeConsumer;

        /**
         * Asynchronous event handler triggered on a client error.
         */
        private Consumer<Exception> errorConsumer;

        /**
         * Socket factory to be used by websockets. Important to allow for custom socket factories when dealing with
         * TLS encryption.
         */
        private SocketFactory socketFactory = SocketFactory.getDefault();

        /**
         * Constant string field name for http cookies
         */
        private final String HTTP_HEADER_FIELD_COOKIE = "cookie";

        /**
         * Set remote endpoint URI
         *
         * @param endpoint remote endpoint URI
         * @return this Builder object
         */
        public Builder setEndpoint(URI endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Set an HTTP Header with initialization message
         *
         * @param name http header name
         * @param value http header value
         * @return this Builder object
         */
        public Builder setHttpHeader(String name, String value) {
            this.httpHeaders.put(name, value);
            return this;
        }

        /**
         * Set multiple HTTP headers with initialization message
         *
         * @param headers
         * @return
         */
        public Builder setHttpHeader(Map<String, String> headers) {
            Set<String> keys = headers.keySet();
            keys.forEach(key -> setHttpHeader(key, headers.get(key)));
            return this;
        }

        /**
         * Set a HTTP cookie with initialization message
         *
         * @param name of the http cookie
         * @param value of the http cookie
         * @return
         */
        public Builder setHttpCookie(String name, String value) {
            String nextCookie = String.join("=", name, value);
            String cookie = nextCookie;

            if (this.httpHeaders.containsKey(HTTP_HEADER_FIELD_COOKIE)) {
                cookie = this.httpHeaders.get(HTTP_HEADER_FIELD_COOKIE);
                cookie = String.join(";", cookie, nextCookie);
            }

            this.httpHeaders.put(HTTP_HEADER_FIELD_COOKIE, cookie);
            return this;
        }

        /**
         * Set multiple HTTP cookies with initialization message
         *
         * @param cookies map of cookie names and values
         * @return this Builder object
         */
        public Builder setHttpCookie(Map<String, String> cookies) {
            Set<String> keys = cookies.keySet();
            keys.forEach(key -> setHttpCookie(key, cookies.get(key)));
            return this;
        }

        /**
         *
         * @param consumer event handler for connection open events
         * @return this Builder object
         */
        public Builder onOpen(Consumer<ServerHandshake> consumer) {
            this.openConsumer = consumer;
            return this;
        }

        /**
         *
         * @param consumer event handler for message events
         * @return this Builder object
         */
        public Builder onMessage(Consumer<String> consumer) {
            this.messageConsumer = consumer;
            return this;
        }

        /**
         *
         * @param consumer event handler for connection close events
         * @return this Builder object
         */
        public Builder onClose(Consumer<Integer> consumer) {
            this.closeConsumer = consumer;
            return this;
        }

        /**
         *
         * @param consumer event handler for connection error events
         * @return this Builder object
         */
        public Builder onError(Consumer<Exception> consumer) {
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
        public Builder setSocketFactory(SocketFactory socketFactory) {
            this.socketFactory = socketFactory;
            return this;
        }

        /**
         * Return built and connected websocket client
         *
         * @return connected websocket client
         * @throws IOException if socket could not be created
         */
        public WebSocketClient connect() throws IOException {
            WebSocketClient instance = new WebSocketClient(this.endpoint,
                this.httpHeaders,
                this.openConsumer,
                this.messageConsumer,
                this.closeConsumer,
                this.errorConsumer);
            instance.setSocket(this.socketFactory.createSocket());
            instance.connect();
            return instance;
        }
    }
}

