package com.deniscoady.flume.websocket.security;

import org.junit.Test;

import javax.net.ssl.SSLContext;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class SSLBuilderTest {

    private File getKeystore() {
        URL url = this.getClass().getResource("/keystore.jks");
        return new File(url.getFile());
    }

    @Test
    public void trustAllCertificates() {
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


        assertTrue(context != null);
    }
}