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


# Examples

## Connecting to Coinbase Websocket API
```
# Example flume.conf using Websocket source

a1.sources  = b1
a1.sinks    = k1
a1.channels = c1

# CHANNELS 
a1.channels.c1.type                = memory
a1.channels.c1.capacity            = 1000
a1.channels.c1.transactionCapacity = 100

# SOURCES
a1.sources.b1.type         = com.deniscoady.flume.websocket.WebSocketSource
a1.sources.b1.endpoint     = wss://ws-feed.pro.coinbase.com
a1.sources.b1.retryDelay   = 5
a1.sources.b1.initMessage  = {"type": "subscribe", "product_ids": ["BTC-USD"], "channels": ["level2"]}
a1.sources.b1.channels     = c1
a1.sources.b1.sslEnabled    = true
a1.sources.b1.trustAllCerts = true
a1.sources.b1.keyStorePath  = conf/keystore.jks
a1.sources.b1.keyStorePass  = changeit

# SINKS
//a1.sinks.e1.type    = logger
//a1.sinks.e1.channel = c1

a1.sinks.k1.type       = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.topic      = coinbase
a1.sinks.k1.brokerList = localhost:9092
a1.sinks.k1.channel    = c1
a1.sinks.k1.batchSize  = 1
a1.sinks.k1.serializer.class = kafka.serializer.DefaultEncoder
```
