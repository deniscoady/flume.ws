# Websocket Source for Apache Flume

## Background
Apache Flume is a simple and effective framework for ingesting small messages in realtime which 
makes a perfect receiver for websocket messages. Unfortunately I couldn't find a library that 
does everything I wanted so I built this one myself. Hopefully it will help others in their 
persuit of building cool things with less effort.

This library was built to provide the following features:
* Connects to plain text `ws://` or secure `wss://` endpoints
* Provides a capability to send a message to a websocket data channel on connection.
* Automatically reconnects after a customizable delay.

# Configurable Properties
| Property      | Required | Default | Description |
|---------------|----------|---------|-------------|
| **endpoint**  | yes      |         | URL endpoint for websocket to establish connection. |
| sslEnabled    | no<sup>1</sup>       | false   | Configure if TLS/SSL encryption should be used on the socket. |
| retryDelay    | no       | 30      | On an unexpected websocket closure, determine how quickly the client should poll attempting to reestablish connection. Duration is in seconds. |
| trustAllCerts | no       | false   | Determine if client should trust ALL TLS certificate authorities including self-signed certificates. If enabled there is a risk of a man-in-the-middle attack and should be used for development purposed only. |
| keyStoreType  | no<sup>2</sup>      | JKS     | Java KeyStore type used to hold trusted certificates. List of valid values can be found for Java 8 at: [Java Cryptography Architecture Standard Algorithm Name Documentation for JDK 8#KeyStore](https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore) |
| keyStorePath  | no<sup>2</sup>      | keystore.jks | Filesystem location of Java KeyStore |
| keyStorePass  | no<sup>2</sup>      | changeit | Password to open and read Java KeyStore |
| initMessage | no |  | After a successful connection, the websocket client will send this message to the remote endpoint. Typically this is used for authentication or subscribing to a message channel. |

### Configuration Additional Notes:
<sup>1</sup> sslEnabled must be set to true for `wss://` protocol usage.

<sup>2</sup> keyStore* properties must be configured if sslEnabled = true and trustAllCerts = false
