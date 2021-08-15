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

package com.deniscoady.flume.websocket.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.*;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLBuilder
{
    private Logger logger = LogManager.getLogger(SSLBuilder.class);
    private SSLContext sslContext;

    public SSLBuilder(
        String keyStoreType,
        String keyStorePath,
        String keyStorePassword,
        boolean trustAllCerts) {
        try {
            KeyStore keyStore = getKeyStore(keyStoreType, keyStorePath, keyStorePassword);
            KeyManagerFactory kmFactory  = getKeyManagerFactory(keyStore, keyStorePassword);
            TrustManager[] trustManagers = getTrustManagers(keyStore, trustAllCerts);
            sslContext = getSSLContext(kmFactory, trustManagers);
        } catch (
            IOException
        |   KeyStoreException
        |   KeyManagementException
        |   UnrecoverableKeyException
        |   CertificateException
        |   NoSuchAlgorithmException ex) {
            logger.error(ex);
        }
    }

    private KeyStore getKeyStore(String keyStoreType, String keyStorePath, String keyStorePassword)
        throws IOException,
               KeyStoreException,
               CertificateException,
               NoSuchAlgorithmException {
        logger.debug("Keystore Details: " +
                "\n\tType: " + keyStoreType +
                "\n\tPath: " + keyStorePath);
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        File keyStoreFile = new File(keyStorePath);
        keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
        return keyStore;
    }

    private KeyManagerFactory getKeyManagerFactory(KeyStore keyStore, String keyStorePassword)
        throws NoSuchAlgorithmException,
               UnrecoverableKeyException,
               KeyStoreException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());
        return kmf;
    }

    private TrustManager[] getTrustManagers(KeyStore keyStore, boolean trustAllCerts)
        throws NoSuchAlgorithmException,
               KeyStoreException {
        TrustManagerFactory tmf = (trustAllCerts)
            ? getEmptyTrustManager()
            : getDefaultTrustManager();
        tmf.init(keyStore);
        return tmf.getTrustManagers();
    }

    private TrustManagerFactory getEmptyTrustManager() {
        logger.debug("Trusting all certificates with EmptyTrustManagerFactory");
        return new EmptyTrustManagerFactory();
    }

    private TrustManagerFactory getDefaultTrustManager()
        throws NoSuchAlgorithmException {
        logger.debug("Using default TrustManagerFactory");
        return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    }

    private SSLContext getSSLContext(KeyManagerFactory kmFactory, TrustManager[] trustManagers)
    throws NoSuchAlgorithmException,
           KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmFactory.getKeyManagers(), trustManagers, new SecureRandom());
        return context;
    }

    public SSLContext getSSLContext() {
        return sslContext;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return sslContext.getSocketFactory();
    }
}
