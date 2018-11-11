package com.deniscoady.flume.websocket.util;

/**
 * Asynchronous event handler triggered on a successful opening of a connection.
 */

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class DefaultWebSocketServerConsumer {

    public static void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {}
    public static void onClose(WebSocket webSocket, Integer code) {}
    public static void onMessage(WebSocket webSocket, String message) {}
    public static void onError(WebSocket webSocket, Exception ex) {}
}
