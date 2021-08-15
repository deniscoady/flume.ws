package com.deniscoady.flume.websocket;

import com.deniscoady.flume.websocket.security.SSLBuilder;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebSocketServerTest {

    private final static Logger logger = Logger.getLogger(WebSocketClientTest.class);

    private final static int TIMEOUT = 2000; // milliseconds

    private final static int SERVER_TCP_PORT = 50009;


    private File getKeystore() {
        URL url = this.getClass().getResource("/keystore.jks");
        return new File(url.getFile());
    }

    @Test
    public void onError() throws InterruptedException, IOException, URISyntaxException {
        final CountDownLatch exceptions = new CountDownLatch(1);
        final InetSocketAddress bindAddress = new InetSocketAddress(
                InetAddress.getLoopbackAddress(),
                SERVER_TCP_PORT);

        WebSocketServer server = new WebSocketServer.Builder()
                .setBindAddress(bindAddress)
                .onError((WebSocket ws, Exception e) -> { exceptions.countDown(); })
                .listen();


        server.onError(null, new Exception());
        assertEquals(0, exceptions.getCount());
        server.stop();
    }

    @Test
    public void setSocketFactory() throws InterruptedException, IOException {
        final InetSocketAddress bindAddress = new InetSocketAddress(
                InetAddress.getLoopbackAddress(),
                SERVER_TCP_PORT);

        File keyStore = getKeystore();
        String keyStoreType = "JKS";
        String keyStorePath = keyStore.getPath();
        String keyStorePass = "changeit";
        boolean trustAllCerts = true;

        SSLBuilder sslBuilder = new SSLBuilder(
                keyStoreType,
                keyStorePath,
                keyStorePass,
                trustAllCerts);

        SSLContext context = sslBuilder.getSSLContext();
        WebSocketServerFactory socketFactory = new DefaultSSLWebSocketServerFactory(context);
        WebSocketServer server = new WebSocketServer.Builder()
                .setBindAddress(bindAddress)
                .setSocketFactory(socketFactory)
                .listen();

        assertEquals(socketFactory, server.getWebSocketFactory());
        server.stop();
    }
}