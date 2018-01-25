/*
 * Copyright 2018 The Messagic Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.messagic.streams.input;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.github.messagic.Event;
import com.github.messagic.streams.StreamsMessageChannelException;

public final class InputPipeThread {

    private static final Logger logger = Logger.getLogger(InputPipeThread.class.getName());

    private final Thread thread;
    private volatile boolean stopped;

    public InputPipeThread(MessageStream messageStream, Consumer<Event> onMessage, Consumer<Exception> onError) {
        thread = new Thread(() -> {
            try {
                while (!stopped) {
                    messageStream.readNextMessage();
                    Message message = messageStream.message();
                    onMessage.accept(message.event());
                }
            } catch (InterruptedIOException e) {
                logger.info("Reading message stream interrupted");
            } catch (IOException e) {
                onError.accept(new StreamsMessageChannelException("Problem during reading input stream", e));
            }
        });
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        stopped = true;
        if (thread != null) {
            thread.interrupt();
        }
    }

}
