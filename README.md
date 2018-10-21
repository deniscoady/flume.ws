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
