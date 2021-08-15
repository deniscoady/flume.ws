# Websocket Source for Apache Flume
[![dcoded](https://circleci.com/gh/dcoded/flume.ws/tree/master.svg?style=svg)](https://circleci.com/gh/dcoded/flume.ws/tree/master)
[![codecov](https://codecov.io/gh/dcoded/flume.ws/branch/master/graph/badge.svg?token=o0ge98Ghku)](https://codecov.io/gh/dcoded/flume.ws)


<img align="center" src="image.jpg" alt="Logo">

## Author Note
Hi there! I've mostly put this up to archive work which can possibly be reused by others. If something is broken or you'd like to see a feature, feel free to leave an issue and I'll take a look.

## Overview
> Flume is a distributed, reliable, and available service for efficiently collecting, aggregating, and moving large amounts of log data. It has a simple and flexible architecture based on streaming data flows. It is robust and fault tolerant with tunable reliability mechanisms and many failover and recovery mechanisms. It uses a simple extensible data model that allows for online analytic application. — http://flume.apache.org

The capabilities of Flume make it a perfect receiver for websocket messages. Unfortunately at the time I couldn't find a plugin library that does everything I wanted so I built this one myself. Hopefully it will help others in their 
pursuit to build cool things with less effort.

This library was built to provide the following features:
* Connects to plain text `ws://` or secure `wss://` endpoints
* Provides a capability to send a message to a websocket data channel on connection.
* Automatically reconnects after a customizable delay.

# Configurable flume.conf Source Properties
| Property      | Required | Default | Type | Description |
|---------------|----------|---------|------|-------------|
| **endpoint**  | yes      |         | string | URL endpoint for websocket to establish connection. |
| sslEnabled    | no<sup>1</sup>       | false   | boolean | Configure if TLS/SSL encryption should be used on the socket. |
| retryDelay    | no       | 30      | integer | On an unexpected websocket closure, determine how quickly the client should poll attempting to reestablish connection. Duration is in seconds. |
| trustAllCerts | no       | false   | boolean | Determine if client should trust ALL TLS certificate authorities including self-signed certificates. If enabled there is a risk of a man-in-the-middle attack and should be used for development purposed only. |
| keyStoreType  | no      |      | string | Java KeyStore type used to hold trusted certificates. List of valid values can be found for Java 8 at: [Java Cryptography Architecture Standard Algorithm Name Documentation for JDK 8#KeyStore](https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore) |
| keyStorePath  | no      |  | string | Filesystem location of Java KeyStore |
| keyStorePass  | no      |  | string | Password to open and read Java KeyStore |
| initMessage | no |  | string | After a successful connection, the websocket client will send this message to the remote endpoint. Typically this is used for authentication or subscribing to a message channel. |
| cookies | no<sup>2</sup> | | strings | Comma-delimited list of keys in cookie array. The keys are not the cookie name, but just define the property key containing the name & value for the cookie. |
| cookie.*.name | no<sup>2</sup> | | string | The name of the cookie |
| cookie.*.value | no<sup>2</sup> | | string | The data of the cookie |

* <sup>1</sup> sslEnabled must be set to true for `wss://` protocol usage.
* <sup>2</sup> A `cookie.*` name/value will only be included if the subparameter key is listed in `cookies`. See [Using HTTP cookies](#using-http-cookies) below for an example.
# Examples

## Get Single Echo Response from echo.websocket.org
```properties
# Example flume.conf using Websocket source

a1.sources  = w1
a1.sinks    = l1
a1.channels = c1

# CHANNELS 
a1.channels.c1.type                = memory
a1.channels.c1.capacity            = 1000
a1.channels.c1.transactionCapacity = 100

# SOURCES
a1.sources.w1.type         = com.deniscoady.flume.websocket.WebSocketSource
a1.sources.w1.endpoint     = ws://echo.websocket.org
a1.sources.w1.retryDelay   = 5
a1.sources.w1.initMessage  = hello world
a1.sources.w1.channels     = c1

# SINKS
a1.sinks.l1.type    = logger
a1.sinks.l1.channel = c1
```

## Connecting to Coinbase Websocket API
```properties
# Example flume.conf using Websocket source

a1.sources  = w1
a1.sinks    = l1
a1.channels = c1

# CHANNELS 
a1.channels.c1.type                = memory
a1.channels.c1.capacity            = 1000
a1.channels.c1.transactionCapacity = 100

# SOURCES
a1.sources.w1.type          = com.deniscoady.flume.websocket.WebSocketSource
a1.sources.w1.endpoint      = wss://ws-feed.pro.coinbase.com
a1.sources.w1.retryDelay    = 5
a1.sources.w1.initMessage   = {"type": "subscribe", "product_ids": ["BTC-USD"], "channels": ["level2"]}
a1.sources.w1.channels      = c1
a1.sources.w1.sslEnabled    = true
a1.sources.w1.trustAllCerts = true

# SINKS
a1.sinks.l1.type    = logger
a1.sinks.l1.channel = c1
```

## Using HTTP cookies
Cookies can be included in the session by adding them to the configuration file like so:
```properties
# ...
a1.sources.w1.cookies              = cookie1,cookie2
a1.sources.w1.cookie.cookie1.name  = some-cookie-name
a1.sources.w1.cookie.cookie1.value = "Data for cookie1 goes here"
a1.sources.w1.cookie.cookie2.name  = another-cookie-name
a1.sources.w1.cookie.cookie2.value = "Data for cookie2 goes here"
# ...
```



# Building and Installing

## TL;DC;DwtR (Too Long; Don't Care; Don't want to Read)
1. Build project and plugin directory for Apache Flume with `sh build.sh`
2. Create a `flume.conf` file
3. Run flume agent: 
```bash
export PLUGINS_DIRECTORY=$(pwd)/plugins.d
export CONFIGS_DIRECTORY=$(pwd)/conf
flume-ng agent                                    \
    --conf          $CONFIGS_DIRECTORY            \
    --conf-file     $CONFIGS_DIRECTORY/flume.conf \
    --plugins-path  $PLUGINS_DIRECTORY            \
    --name a1
```

## Building the Project
This project uses Apache Maven to build so simply running `mvn package` should be enough to get a library jar. Before this source library can be found by Apache Flume it must be added to the Flume plugins directory. 

**NOTE:** The latest instructions on this can be found here: https://flume.apache.org/FlumeUserGuide.html#installing-third-party-plugins.

## Installing third-party libraries for Apache Flume
Flume has a fully plugin-based architecture. While Flume ships with many out-of-the-box sources, channels, sinks, serializers, and the like, many implementations exist which ship separately from Flume.

While it has always been possible to include custom Flume components by adding their jars to the FLUME_CLASSPATH variable in the flume-env.sh file, Flume now supports a special directory called plugins.d which automatically picks up plugins that are packaged in a specific format. This allows for easier management of plugin packaging issues as well as simpler debugging and troubleshooting of several classes of issues, especially library dependency conflicts.

The plugins.d directory is located at `$FLUME_HOME/plugins.d`. At startup time, the flume-ng start script looks in the plugins.d directory for plugins that conform to the below format and includes them in proper paths when starting up java. 

**NOTE:** The plugin directory can also be defined when executing the flume-ng command with the `--plugins-path` parameter.

### Creating the custom plugin directory
Each plugin (subdirectory) within plugins.d can have up to three sub-directories:

* lib - the plugin’s jar(s)
* libext - the plugin’s dependency jar(s)
* native - any required native libraries, such as .so files

Example of the websocket plugin within the plugins.d directory:

```
plugins.d/
plugins.d/flume-websocket-source/
plugins.d/flume-websocket-source/lib/websocket-1.0.jar
plugins.d/flume-websocket-source/libext/dependency.jar
```
