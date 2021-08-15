package com.deniscoady.flume.websocket;

import org.apache.flume.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SourceConfigurationTest {

    private final static String ENDPOINT = "ws://localhost:8080";
    private final static String INIT_MESSAGE = "test";
    private final static Integer RETRY_DELAY = 5;

    private final static String TRUE  = "true";
    private final static String FALSE = "false";

    @Test
    public void getEndpointAddress() throws URISyntaxException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertEquals(ENDPOINT, conf.getEndpointAddress().toString());
    }

    @Test
    public void hasInitializationMessage() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);
        parameters.put("initMessage", INIT_MESSAGE);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertTrue(conf.hasInitializationMessage());
    }

    @Test
    public void hasNoInitializationMessage() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertFalse(conf.hasInitializationMessage());
    }

    @Test
    public void getInitializationMessage() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);
        parameters.put("initMessage", INIT_MESSAGE);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertEquals(INIT_MESSAGE, conf.getInitializationMessage());
    }

    @Test
    public void getRetryDelay() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("retryDelay", RETRY_DELAY.toString());
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertEquals(RETRY_DELAY, conf.getRetryDelay());
    }

    @Test
    public void isSecure() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sslEnabled", TRUE);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertTrue(conf.isSecure());
    }

    @Test
    public void isNotSecure() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sslEnabled", FALSE);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertFalse(conf.isSecure());
    }

    @Test
    public void getUnsetKeyStoreType() {
        Map<String, String> parameters = new HashMap<>();
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertNull(conf.getKeyStoreType());
    }

    @Test
    public void getUnsetKeyStorePath() {
        Map<String, String> parameters = new HashMap<>();
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertNull(conf.getKeyStorePath());
    }

    @Test
    public void getKeyStorePassword() {
        Map<String, String> parameters = new HashMap<>();
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertNull(conf.getKeyStorePassword());
    }

    @Test
    public void trustAllCertificates() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("trustAllCerts", TRUE);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertTrue(conf.trustAllCertificates());
    }

    @Test
    public void trustSomeCertificates() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("trustAllCerts", FALSE);
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertFalse(conf.trustAllCertificates());
    }

    @Test
    public void getCookies() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("some-cookie-name", "Data for cookie1 goes here");
        cookies.put("another-cookie-name", "Data for cookie2 goes here");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("cookies", String.join(",", cookies.keySet()));
        cookies.forEach((String key, String value) -> {
            String nameParameter = String.join(".","cookie", key, "name");
            String valueParameter = String.join(".","cookie", key, "value");
            parameters.put(nameParameter, key);
            parameters.put(valueParameter, value);
        });

        System.out.println(parameters.get("cookies"));

        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertEquals(cookies, conf.getCookies());
    }

    @Test
    public void testWithNoCookies() {
        Map<String, String> cookies = new HashMap<>();
        Map<String, String> parameters = new HashMap<>();
        SourceConfiguration conf = new SourceConfiguration(new Context(parameters));
        assertEquals(cookies, conf.getCookies());
    }
}