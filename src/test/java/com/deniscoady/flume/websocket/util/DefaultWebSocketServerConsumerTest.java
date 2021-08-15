package com.deniscoady.flume.websocket.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultWebSocketServerConsumerTest {

    @Test
    public void onOpen() {
        DefaultWebSocketServerConsumer consumer = new DefaultWebSocketServerConsumer();
        consumer.onOpen(null, null);
    }

    @Test
    public void onClose() {
        DefaultWebSocketServerConsumer consumer = new DefaultWebSocketServerConsumer();
        consumer.onClose(null, null);
    }

    @Test
    public void onMessage() {
        DefaultWebSocketServerConsumer consumer = new DefaultWebSocketServerConsumer();
        consumer.onMessage(null, null);
    }

    @Test
    public void onError() {
        DefaultWebSocketServerConsumer consumer = new DefaultWebSocketServerConsumer();
        consumer.onError(null, null);
    }
}