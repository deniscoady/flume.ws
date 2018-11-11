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

package com.deniscoady.flume.websocket;

import org.apache.flume.Context;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper configuration class to parse valid properties from Flume Context
 */
public class SinkConfiguration {

    public final static String CONTEXT_KEY_LISTENING_HOST  = "host";
    public final static String CONTEXT_KEY_LISTENING_PORT  = "port";

    /**
     * Enable TLS Encryption (sslEnabled)
     *
     * Required: no
     * Default : false
     *
     * Configure if TLS/SSL encryption should be used on the socket.
     */
    public final static String CONTEXT_KEY_SSL_ENABLED   = "sslEnabled";

    /**
     * Trust All TLS Certificates (trustAllCerts)
     *
     * Required: no
     * Default : false
     *
     * Determine if client should trust ALL TLS certificate authorities including self-signed certificates.
     * If enabled there is a risk of a man-in-the-middle attack.
     */
    public final static String CONTEXT_KEY_KEYSTORE_OPEN = "trustAllCerts";

    /**
     * Java KeyStore Type (keyStoreType)
     *
     * Required: no except if sslEnabled = true and trustAllCerts = false
     * Default : JKS
     *
     * Java KeyStore type used to hold trusted certificates. List of valid values can be found for Java 8 at:
     * https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
     */
    public final static String CONTEXT_KEY_KEYSTORE_TYPE = "keyStoreType";

    /**
     * Java KeyStore File Path (keyStorePath)
     *
     * Required: no except if sslEnabled = true and trustAllCerts = false
     * Default : keystore.jks
     *
     * Filesystem location of Java KeyStore
     */
    public final static String CONTEXT_KEY_KEYSTORE_PATH = "keyStorePath";

    /**
     * Java KeyStore Password (keyStorePass)
     *
     * Required: no except if sslEnabled = true and trustAllCerts = false
     * Default : changeit
     *
     * Password to open and read Java KeyStore
     */
    public final static String CONTEXT_KEY_KEYSTORE_PASS = "keyStorePass";

    /**
     * Default configuration values.
     *
     * CONTEXT_DEFAULT_* is the default value for the CONTEXT_KEY_* property.
     */
    public final static String CONTEXT_DEFAULT_LISTENING_HOST = "0.0.0.0";
    public final static Integer CONTEXT_DEFAULT_LISTENING_PORT = 8080;
    public final static boolean CONTEXT_DEFAULT_SSL_ENABLED   = false;
    public final static String CONTEXT_DEFAULT_KEYSTORE_TYPE  = null;
    public final static String CONTEXT_DEFAULT_KEYSTORE_PATH  = null;
    public final static String CONTEXT_DEFAULT_KEYSTORE_PASS  = null;
    public final static boolean CONTEXT_DEFAULT_KEYSTORE_OPEN = false;

    private final String  host;
    private final Integer port;
    private final Boolean sslEnabled;
    private final String  keystoreType;
    private final String  keystorePath;
    private final String  keystorePass;
    private final boolean keystoreOpen;

    /**
     * Parse configuration settings from Flume context
     *
     * @param context Flume context
     */
    public SinkConfiguration(Context context) {
        host         = context.getString(CONTEXT_KEY_LISTENING_HOST, CONTEXT_DEFAULT_LISTENING_HOST);
        port         = context.getInteger(CONTEXT_KEY_LISTENING_PORT, CONTEXT_DEFAULT_LISTENING_PORT);
        sslEnabled   = context.getBoolean(CONTEXT_KEY_SSL_ENABLED, CONTEXT_DEFAULT_SSL_ENABLED);
        keystoreType = context.getString(CONTEXT_KEY_KEYSTORE_TYPE, CONTEXT_DEFAULT_KEYSTORE_TYPE);
        keystorePath = context.getString(CONTEXT_KEY_KEYSTORE_PATH, CONTEXT_DEFAULT_KEYSTORE_PATH);
        keystorePass = context.getString(CONTEXT_KEY_KEYSTORE_PASS, CONTEXT_DEFAULT_KEYSTORE_PASS);
        keystoreOpen = context.getBoolean(CONTEXT_KEY_KEYSTORE_OPEN, CONTEXT_DEFAULT_KEYSTORE_OPEN);
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    /**
     * Check to see if TLS encryption was enabled.
     *
     * @return true if sslEnabled property is true and TLS encryption will be used
     */
    public Boolean isSecure() {
        return sslEnabled;
    }

    /**
     * Get Java KeyStore type.
     *
     * @return Java KeyStore type
     */
    public String getKeyStoreType() {
        return keystoreType;
    }

    /**
     * Get Java KeyStore path.
     *
     * @return Java KeyStore path
     */
    public String getKeyStorePath() {
        return keystorePath;
    }

    /**
     * Get Java KeyStore password.
     *
     * @return Java KeyStore password
     */
    public String getKeyStorePassword() {
        return keystorePass;
    }

    /**
     * Check to see if all TLS certificates should be trusted.
     *
     * @return true if trustAllCerts is true and all TLS certificates will be trusted
     */
    public Boolean trustAllCertificates() {
        return keystoreOpen;
    }
}
