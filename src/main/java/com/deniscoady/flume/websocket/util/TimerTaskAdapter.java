package com.deniscoady.flume.websocket.util;

import java.util.TimerTask;

public class TimerTaskAdapter extends TimerTask {

    private final Runnable task;

    public TimerTaskAdapter(Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        this.task.run();
    }
}
