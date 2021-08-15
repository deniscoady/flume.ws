package com.deniscoady.flume.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebSocketClientTest {

    private final static Logger logger = LogManager.getLogger(WebSocketClientTest.class);

    private final static int TIMEOUT = 10000; // milliseconds

    private final static int SERVER_TCP_PORT = 50009;

    private WebSocketServer server;

    @Before
    public void setUp() {
        server = new WebSocketServer.Builder()
                // Bind to 0.0.0.0:55055
                .setBindAddress(new InetSocketAddress(
                        InetAddress.getLoopbackAddress(),
                        SERVER_TCP_PORT))
                .listen();
    }

    @After
    public void cleanUp() throws InterruptedException, IOException {
        server.stop();
    }

    @Test
    public void onOpen() throws IOException, URISyntaxException, InterruptedException {
        CountDownLatch connected = new CountDownLatch(1);
        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .onOpen((ServerHandshake handshake) -> connected.countDown())
                .onClose(code -> {})
                .onError(ex -> ex.printStackTrace())
                .connect();

        connected.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, connected.getCount());
    }

    @Test
    public void onMessage() throws URISyntaxException, IOException, InterruptedException {
        String testMessage = new Date().toString();

        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch pingPong  = new CountDownLatch(1);

        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .onOpen((ServerHandshake handshake) -> connected.countDown())
                .onMessage(message -> {
                    assertEquals(testMessage, message);
                    pingPong.countDown();
                })
                .onClose(code -> {})
                .onError(ex -> ex.printStackTrace())
                .connect();

        connected.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, connected.getCount());
        client.send(testMessage);
        pingPong.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, pingPong.getCount());
    }

    @Test
    public void onClose() throws InterruptedException, URISyntaxException, IOException {
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch disconnected = new CountDownLatch(1);
        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .onOpen((ServerHandshake handshake) -> connected.countDown())
                .onClose(code -> disconnected.countDown())
                .onError(ex -> ex.printStackTrace())
                .onMessage(message -> {})
                .connect();

        connected.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, connected.getCount());
        client.close();
        disconnected.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, disconnected.getCount());
    }

    @Test
    public void onError() throws URISyntaxException, IOException, InterruptedException {
        String testMessage = new Date().toString();
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch errored   = new CountDownLatch(1);

        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .onOpen((ServerHandshake handshake) -> connected.countDown())
                .onClose(code -> {})
//                .onMessage(message -> {})
                .onError(ex -> errored.countDown())
                .connect();

        connected.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertTrue(client.isOpen());
        assertEquals(0, connected.getCount());
        // trigger error by sending message without setting a onMessage handler.
        client.send("");
        errored.await(TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, errored.getCount());
    }

    @Test
    public void setHttpHeaders() throws URISyntaxException, IOException {
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put("a", "1");
        httpHeaders.put("b", "2");
        httpHeaders.put("c", "3");


        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .setHttpHeader(httpHeaders)
                .onError(ex -> ex.printStackTrace())
                .onOpen(handshake -> {})
                .onClose(code -> {})
                .onMessage(message -> {})
                .connect();

        assertEquals(httpHeaders, client.getHttpHeaders());
    }

    @Test
    public void setHttpCookies() throws URISyntaxException, IOException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("a", "1");
        cookies.put("b", "2");
        cookies.put("c", "3");

        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .setHttpCookie(cookies)
                .onError(ex -> ex.printStackTrace())
                .onOpen(handshake -> {})
                .onClose(code -> {})
                .onMessage(message -> {})
                .connect();

        String cookie = client.getHttpHeaders().get("cookie");
        assertEquals("a=1;b=2;c=3", cookie);
    }

    @Test
    public void setSocketFactory() throws URISyntaxException, IOException {
        WebSocketClient client = new WebSocketClient.Builder()
                .setEndpoint(new URI("ws://localhost:" + SERVER_TCP_PORT))
                .setSocketFactory(SocketFactory.getDefault())
                .onError(ex -> ex.printStackTrace())
                .onOpen(handshake -> {})
                .onClose(code -> {})
                .onMessage(message -> {})
                .connect();
    }
}

