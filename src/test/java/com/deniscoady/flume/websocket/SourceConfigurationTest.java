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

    private SourceConfiguration configuration;

    private final static String ENDPOINT = "ws://localhost:8080";

    private final static String INIT_MESSAGE =
        "{" +
            "\"type\": \"subscribe\", " +
            "\"product_ids\": [\"BTC-USD\"], " +
            "\"channels\": [\"level2\"]" +
        "}";

    private final static Integer RETRY_DELAY = 5;

    private final static Boolean IS_SECURE = false;

    private final static Boolean TRUST_ALL_CERTS = true;

    private final static Map<String, String> COOKIES = new HashMap<String, String>() {
        {
            put("some-cookie-name", "Data for cookie1 goes here");
            put("another-cookie-name", "Data for cookie2 goes here");
        }
    };

    @Before
    public void setUp() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("endpoint", ENDPOINT);
        parameters.put("retryDelay", RETRY_DELAY.toString());
        parameters.put("initMessage", INIT_MESSAGE);
        parameters.put("sslEnabled", IS_SECURE.toString());
        parameters.put("trustAllCerts", TRUST_ALL_CERTS.toString());
        parameters.put("cookies", "cookie1, cookie2");
        parameters.put("cookie.cookie1.name", "some-cookie-name");
        parameters.put("cookie.cookie1.value", "Data for cookie1 goes here");
        parameters.put("cookie.cookie2.name", "another-cookie-name");
        parameters.put("cookie.cookie2.value", "Data for cookie2 goes here");
        configuration = new SourceConfiguration(new Context(parameters));
    }

    @Test
    public void getEndpointAddress() throws URISyntaxException {
        assertEquals(ENDPOINT, configuration.getEndpointAddress().toString());
    }

    @Test
    public void hasInitializationMessage() {
        assertEquals(INIT_MESSAGE != null, configuration.hasInitializationMessage());
    }

    @Test
    public void getInitializationMessage() {
        assertEquals(INIT_MESSAGE, configuration.getInitializationMessage());
    }

    @Test
    public void getRetryDelay() {
        assertEquals(RETRY_DELAY, configuration.getRetryDelay());
    }

    @Test
    public void isSecure() {
        assertEquals(IS_SECURE, configuration.isSecure());
    }

    @Test
    public void getKeyStoreType() {
        assertNull(configuration.getKeyStoreType());
    }

    @Test
    public void getKeyStorePath() {
        assertNull(configuration.getKeyStorePath());
    }

    @Test
    public void getKeyStorePassword() {
        assertNull(configuration.getKeyStorePassword());
    }

    @Test
    public void trustAllCertificates() {
        assertEquals(TRUST_ALL_CERTS, configuration.trustAllCertificates());
    }

    @Test
    public void getCookies() {
        assertEquals(COOKIES, configuration.getCookies());
    }
}