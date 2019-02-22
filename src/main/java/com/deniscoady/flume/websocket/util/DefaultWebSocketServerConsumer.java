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

package com.deniscoady.flume.websocket.util;

/**
 * Asynchronous event handler triggered on a successful opening of a connection.
 */

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * A placeholder implementation for events which the instance does not need to
 * react to.
 */
public class DefaultWebSocketServerConsumer {

    public static void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "open" event.
         */
    }

    public static void onClose(WebSocket webSocket, Integer code) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "close" event.
         */
    }

    public static void onMessage(WebSocket webSocket, String message) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "message" event.
         */
    }

    public static void onError(WebSocket webSocket, Exception ex) {
        /**
         * This method is empty to act as a "do nothing" operation in cases where
         * the end usage of the WebSocket server does not require notification of
         * an "error" event.
         */
    }
}
