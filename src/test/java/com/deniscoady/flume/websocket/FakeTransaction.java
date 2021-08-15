package com.deniscoady.flume.websocket;


import org.apache.flume.*;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.lifecycle.LifecycleState;

import java.util.LinkedList;
import java.util.List;

public class FakeTransaction implements Transaction {

    @Override
    public void begin() {
        /**
         * Implementation not required.
         */
    }

    @Override
    public void commit() {
        /**
         * Implementation not required.
         */
    }

    @Override
    public void rollback() {
        /**
         * Implementation not required.
         */
    }

    @Override
    public void close() {
        /**
         * Implementation not required.
         */
    }
}


class TestChannel implements Channel {

    private String name = "";
    private LifecycleState state = LifecycleState.IDLE;
    private List<String> messages = new LinkedList<>();

    @Override
    public void put(Event event) throws ChannelException {
        byte[] data = event.getBody();
        messages.add(new String(data));
    }

    @Override
    public Event take() throws ChannelException {
        if (messages.isEmpty() == false) {
            String message = messages.get(0);
            messages.remove(0);
            Event event = new SimpleEvent();
            event.setBody(message.getBytes());
            return event;
        }
        return null;
    }

    @Override
    public Transaction getTransaction() {
        return new FakeTransaction();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        state = LifecycleState.START;
    }

    @Override
    public void stop() {
        state = LifecycleState.STOP;
    }

    @Override
    public LifecycleState getLifecycleState() {
        return state;
    }
}

class TestChannelSelector implements ChannelSelector {

    private String name = "";
    private List<Channel> required = new LinkedList<>();
    private List<Channel> optional = new LinkedList<>();

    public TestChannelSelector() {
        required.add(new TestChannel());
    }

    @Override
    public void setChannels(List<Channel> channels) {
        /**
         * Implementation not required.
         */
    }

    @Override
    public List<Channel> getRequiredChannels(Event event) {
        return required;
    }

    @Override
    public List<Channel> getOptionalChannels(Event event) {
        return optional;
    }

    @Override
    public List<Channel> getAllChannels() {
        return required;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void configure(Context context) {
        /**
         * Implementation not required.
         */
    }
}