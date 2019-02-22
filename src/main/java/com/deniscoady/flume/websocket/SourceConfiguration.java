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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper configuration class to parse valid properties from Flume Context
 */
public class SourceConfiguration {
    /**
     * Endpoint (endpoint)
     *
     * Required: yes
     * Default: null
     *
     * URL endpoint for websocket to establish connection.
     * Example: ws://example.com or wss://example.com if CONTEXT_SSL_ENABLED is true
     */
    public final static String CONTEXT_KEY_ENDPOINT_URI  = "endpoint";

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
     * Retry Delay (retryDelay)
     *
     * Required: no
     * Default : 30
     *
     * On an unexpected websocket closure, determine how quickly the client should poll
     * attempting to reestablish connection. Duration is in seconds.
     */
    public final static String CONTEXT_KEY_RETRY_DELAY   = "retryDelay";

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
     * Connection Initialization Message (initMessage)
     *
     * Required: no
     * Default : null
     *
     * After a successful connection, the websocket client will send this message to the remote endpoint. Typically
     * this is used for authentication or subscribing to a message channel.
     */
    public final static String CONTEXT_KEY_INIT_MESSAGE  = "initMessage";

    /**
     * Comma-delimted list of HTTP cookie names to be passed to remote server
     *
     * Required: no
     * Default : null
     *
     * After a successful connection, the websocket client will create each HTTP cookie and forward them with the
     * initial message.
     *
     * Cookies are defined in the typical Flume convention of:
     *
     *  .cookies = cookie1,cookie2,...
     *  .cookie.cookie1.name = Cookie 1 Name
     *  .cookie.cookie1.data = Cookie 1 Data
     *  .cookie.cookie2.name = Cookie 2 Name
     *  .cookie.cookie2.data = Cookie 2 Data
     */
    public final static String CONTEXT_KEY_COOKIE_NAMES  = "cookies";

    /**
     * Array of key/value pairs containing an HTTP cookie each
     *
     * Required: no
     * Default : null
     *
     * After a successful connection, the websocket client will create each HTTP cookie defined with name of key
     *
     * Cookies are defined in the typical Flume convention of:
     *
     *  .cookies = cookie1,cookie2,...
     *  .cookie.cookie1.name = Cookie 1 Name
     *  .cookie.cookie1.data = Cookie 1 Data
     *  .cookie.cookie2.name = Cookie 2 Name
     *  .cookie.cookie2.data = Cookie 2 Data
     *
     */
    public final static String CONTEXT_KEY_COOKIE_ARRAY  = "cookie";

    /**
     * Default configuration values.
     *
     * CONTEXT_DEFAULT_* is the default value for the CONTEXT_KEY_* property.
     */
    public final static String  CONTEXT_DEFAULT_ENDPOINT_URI  = null;
    public final static String  CONTEXT_DEFAULT_INIT_MESSAGE  = null;
    public final static boolean CONTEXT_DEFAULT_SSL_ENABLED   = false;
    public final static String  CONTEXT_DEFAULT_KEYSTORE_TYPE = null;
    public final static String  CONTEXT_DEFAULT_KEYSTORE_PATH = null;
    public final static String  CONTEXT_DEFAULT_KEYSTORE_PASS = null;
    public final static boolean CONTEXT_DEFAULT_KEYSTORE_OPEN = false;
    public final static Integer CONTEXT_DEFAULT_RETRY_DELAY   = 30;
    public final static String  CONTEXT_DEFAULT_COOKIE_NAMES  = null;
    public final static Map<String, String> CONTEXT_DEFAULT_COOKIE_ARRAY = new HashMap<>();


    private final String  endpoint;
    private final String  initMessage;
    private final Integer retryDelay;
    private final Boolean sslEnabled;
    private final String  keystoreType;
    private final String  keystorePath;
    private final String  keystorePass;
    private final Boolean keystoreOpen;
    private final String  cookieNames;
    private final Map<String, String> cookieArray;

    /**
     * Parse configuration settings from Flume context
     *
     * @param context Flume context
     */
    public SourceConfiguration(Context context) {
        endpoint     = context.getString(CONTEXT_KEY_ENDPOINT_URI, CONTEXT_DEFAULT_ENDPOINT_URI);
        initMessage  = context.getString(CONTEXT_KEY_INIT_MESSAGE, CONTEXT_DEFAULT_INIT_MESSAGE);
        retryDelay   = context.getInteger(CONTEXT_KEY_RETRY_DELAY, CONTEXT_DEFAULT_RETRY_DELAY);
        sslEnabled   = context.getBoolean(CONTEXT_KEY_SSL_ENABLED, CONTEXT_DEFAULT_SSL_ENABLED);
        keystoreType = context.getString(CONTEXT_KEY_KEYSTORE_TYPE, CONTEXT_DEFAULT_KEYSTORE_TYPE);
        keystorePath = context.getString(CONTEXT_KEY_KEYSTORE_PATH, CONTEXT_DEFAULT_KEYSTORE_PATH);
        keystorePass = context.getString(CONTEXT_KEY_KEYSTORE_PASS, CONTEXT_DEFAULT_KEYSTORE_PASS);
        keystoreOpen = context.getBoolean(CONTEXT_KEY_KEYSTORE_OPEN, CONTEXT_DEFAULT_KEYSTORE_OPEN);
        cookieNames  = context.getString(CONTEXT_KEY_COOKIE_NAMES, CONTEXT_DEFAULT_COOKIE_NAMES);
        cookieArray  = context.getSubProperties(CONTEXT_KEY_COOKIE_ARRAY + ".");
    }

    /**
     * Get websocket server endpoint.
     *
     * @return address of websocket server endpoint
     * @throws URISyntaxException if endpoint property is not set
     */
    public URI getEndpointAddress() throws URISyntaxException {
        return new URI(endpoint);
    }

    /**
     * Check to see if user provided a non-null initialization message.
     *
     * @return true if initialization message is defined
     */
    public Boolean hasInitializationMessage() {
        return initMessage != null;
    }

    /**
     * Get initialization message or null if none was configured
     *
     * @return initialization message or null if not configured
     */
    public String getInitializationMessage() {
        return initMessage;
    }

    /**
     * Get retry delay in seconds.
     *
     * @return retry delay in seconds
     */
    public Integer getRetryDelay() {
        return retryDelay;
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

    /**
     * Get HTTP cookies listed and defined
     *
     * @return a string map of cookie names and data
     */
    public Map<String, String> getCookies() {
        Map<String, String> cookies = new HashMap<>();
        if (cookieNames != CONTEXT_DEFAULT_COOKIE_NAMES) {
            Arrays.stream(cookieNames.split("\\s*,\\s*"))
                    .filter(id -> cookieArray.containsKey(id + ".name"))
                    .filter(id -> cookieArray.containsKey(id + ".value"))
                    .forEach(id -> cookies.put(
                            cookieArray.get(id + ".name"),
                            cookieArray.get(id + ".value")));
        }

        return cookies;
    }
}
