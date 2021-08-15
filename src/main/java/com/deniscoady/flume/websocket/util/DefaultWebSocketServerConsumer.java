package com.deniscoady.flume.websocket.util;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class DefaultWebSocketServerConsumer implements WebSocketServerConsumer {
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "open" event.
         */
    }

    @Override
    public void onClose(WebSocket webSocket, Integer code) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "close" event.
         */
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "message" event.
         */
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "error" event.
         */
    }
}
