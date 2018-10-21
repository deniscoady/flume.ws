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

import com.deniscoady.flume.websocket.util.Delay;
import com.deniscoady.flume.websocket.internal.TestChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.junit.Test;

import java.util.Date;

public class WebSocketSourceTest {

    @Test
    public void basicEchoTest() {
        Context context = new Context();
        context.put(Configuration.CONTEXT_KEY_ENDPOINT_URI, "ws://echo.websocket.org");
        context.put(Configuration.CONTEXT_KEY_SSL_ENABLED, "false");
        context.put(Configuration.CONTEXT_KEY_INIT_MESSAGE, "hello");
        testConfiguration(context);
    }

    private void testConfiguration(Context context) {
        WebSocketSource source = new WebSocketSource();
        ChannelProcessor processor = new ChannelProcessor(new TestChannelSelector());
        source.setChannelProcessor(processor);
        source.configure(context);
        source.start();
        final Date startTime = new Date();
        Delay.awaitCondition(1000, () -> waited(startTime));
        source.stop();
    }

    private Boolean waited(Date startTime) {
        Long current = new Date().getTime();
        Long started = startTime.getTime();
        return current - started < 30_000;
    }
}