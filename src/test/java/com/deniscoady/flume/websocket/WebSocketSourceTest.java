package com.deniscoady.flume.websocket;

import org.apache.flume.*;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class WebSocketSourceTest {

    private final static int SERVER_TCP_PORT = 50009;
    private final static String ENDPOINT = "ws://localhost:" + SERVER_TCP_PORT;
    private final static String SECURE_ENDPOINT = "wss://localhost:" + SERVER_TCP_PORT;

    private final static String TRUE = "true";
    private final static String FALSE = "false";

    private WebSocketServer server;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        InetSocketAddress bindAddress = new InetSocketAddress(
                InetAddress.getLoopbackAddress(),
                SERVER_TCP_PORT);

        server = new WebSocketServer.Builder()
                .setBindAddress(bindAddress)
                .listen();
    }

    @After
    public void cleanUp() throws InterruptedException, IOException {
        server.stop();
    }

    @Test
    public void basicStartAndStop() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);

        ChannelProcessor processor = new ChannelProcessor(null);
        WebSocketSource source = new WebSocketSource();
        source.configure(new Context(parameters));
        source.setChannelProcessor(processor);
        source.start();
        assertTrue(source.getLifecycleState() == LifecycleState.START);
        assertTrue(source.getLifecycleState() != LifecycleState.ERROR);
        source.stop();
        assertTrue(source.getLifecycleState() == LifecycleState.STOP);
        assertTrue(source.getLifecycleState() != LifecycleState.ERROR);
    }

    @Test
    public void secureStartAndStop() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", SECURE_ENDPOINT);
        parameters.put("sslEnabled", TRUE);
        parameters.put("trustAllCerts", TRUE);


        ChannelProcessor processor = new ChannelProcessor(null);
        WebSocketSource source = new WebSocketSource();
        source.configure(new Context(parameters));
        source.setChannelProcessor(processor);
        source.start();
        assertTrue(source.getLifecycleState() == LifecycleState.START);
        assertTrue(source.getLifecycleState() != LifecycleState.ERROR);
        source.stop();
        assertTrue(source.getLifecycleState() == LifecycleState.STOP);
        assertTrue(source.getLifecycleState() != LifecycleState.ERROR);
    }

    @Test
    public void invalidStartAndStop() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", null);

        try {
            ChannelProcessor processor = new ChannelProcessor(null);
            WebSocketSource source = new WebSocketSource();
            source.configure(new Context(parameters));
            source.setChannelProcessor(processor);
            source.start();
            fail("Should not start without a proper endpoint address.");
        } catch (Exception ex) {
            // This test is expected to throw an exception.
        }
    }

    @Test
    public void useKeyStore() throws KeyStoreException, NoSuchAlgorithmException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", SECURE_ENDPOINT);
        parameters.put("sslEnabled", TRUE);
        parameters.put("keyStoreType", "JKS");
        parameters.put("keyStorePath", "src/test/resources/keystore.jks");
        parameters.put("keyStorePass", "changeit");

        ChannelProcessor processor = new ChannelProcessor(null);
        WebSocketSource source = new WebSocketSource();
        source.configure(new Context(parameters));
        source.setChannelProcessor(processor);
        source.start();
    }

    @Test
    public void useKeyStoreMissingPath() throws InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", SECURE_ENDPOINT);
        parameters.put("initMessage", "ping");
        parameters.put("sslEnabled", TRUE);
        parameters.put("keyStoreType", "JKS");
        parameters.put("keyStorePass", "changeit");

        ChannelSelector selector = new TestChannelSelector();
        ChannelProcessor processor = new ChannelProcessor(selector);
        WebSocketSource source = new WebSocketSource();
        source.configure(new Context(parameters));
        source.setChannelProcessor(processor);

        source.start();
        Thread.sleep(1000);
        source.stop();

        List<Channel> channels = selector.getAllChannels();
        Channel c = channels.get(0);
        Event event = c.take();
        assertNull(event);
    }

    @Test
    public void echoMessage() throws KeyStoreException, NoSuchAlgorithmException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);
        parameters.put("initMessage", "ping");

        ChannelSelector selector = new TestChannelSelector();
        ChannelProcessor processor = new ChannelProcessor(selector);
        WebSocketSource source = new WebSocketSource();
        source.configure(new Context(parameters));
        source.setChannelProcessor(processor);

        source.start();
        Thread.sleep(2000);
        source.stop();

        List<Channel> channels = selector.getAllChannels();
        Channel c = channels.get(0);
        Event event = c.take();
        assertNotNull(event);
        String message = new String(event.getBody());
        assertEquals("ping", message);
    }
}

