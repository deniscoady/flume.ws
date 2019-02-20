package com.deniscoady.flume.websocket.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;

public class TimerTaskAdapterTest {

    private final static long INTERVAL_MS = 500;
    private final static long ITERATIONS  = 3;

    private Timer timer;
    private long  value;

    @Before
    public void setUp() throws Exception {
        timer = new Timer();
        value = 0;
    }

    @After
    public void tearDown() throws Exception {
        timer.cancel();
    }

    @Test
    public void encapsulateLambdaFunction() throws InterruptedException {
        TimerTask task = new TimerTaskAdapter(() -> value++ );
        timer.schedule(task, 0, INTERVAL_MS);
        iterations();
    }

    @Test
    public void encapsulateMethodCall() throws InterruptedException {
        TimerTask task = new TimerTaskAdapter(this::testMethod);
        timer.schedule(task, 0, INTERVAL_MS);
        iterations();
    }

    private void iterations() throws InterruptedException {
        for (int i = 1; i < ITERATIONS; i++) {
            Thread.sleep(INTERVAL_MS - 100);
            assertEquals(i, value);
        }
    }

    private void testMethod() {
        value++;
    }
}