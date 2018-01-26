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
package com.github.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import com.github.messagic.Event;
import com.github.messagic.MessageChannel;
import com.github.messagic.streams.eventbus.EventBus;
import com.github.messagic.streams.input.InputPipeThread;
import com.github.messagic.streams.input.MessageStream;
import com.github.messagic.streams.output.MessageFactory;
import com.github.messagic.streams.output.OutputPipe;

/**
 * MessageChannel implementation using Input and OutputStream. Protocol is text-based in UTF-8 encoding. Binary messages
 * are encoded using Base64. Every message is separated using new line "\n".
 * <p>
 * More info about Protocol here:
 * <a href="https://github.com/messagic/messagic-java/blob/master/implementations/streams/README.md">README.md</a>
 */
public final class StreamsMessageChannel implements MessageChannel {

    private final InputPipeThread inputPipeThread;
    private final OutputPipe outputPipe;
    private final EventBus eventsBus;

    private State state = State.NEW;

    public StreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public StreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        eventsBus = new EventBus();
        MessageStream messageStream = limits.messageStream(input, this);
        inputPipeThread = new InputPipeThread(messageStream, eventsBus, exception -> {
            eventsBus.accept(new ErrorEvent(this, exception));
            stop();
        });
        MessageFactory messageFactory = limits.messageFactory();
        outputPipe = new OutputPipe(output, messageFactory, exception -> {
            eventsBus.accept(new ErrorEvent(this, exception));
            stop();
        });
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener) {
        eventsBus.addListener(eventClass, listener);
    }

    @Override
    public <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener) {
        eventsBus.removeListener(eventClass, listener);
    }

    @Override
    public synchronized void start() {
        if (state == State.NEW) {
            inputPipeThread.start();
            state = State.STARTED;
            eventsBus.start();
            eventsBus.accept(new StartedEvent(this));
        } else if (state == State.STOPPED) {
            throw new IllegalStateException("Can't start channel which was stopped before");
        }
    }

    @Override
    public void send(String textMessage) {
        outputPipe.send(textMessage);
    }

    @Override
    public void send(byte[] binaryMessage) {
        outputPipe.send(binaryMessage);
    }

    @Override
    public synchronized void stop() {
        if (state == State.STARTED) {
            try {
                this.inputPipeThread.stop();
            } finally {
                this.outputPipe.stop();
            }
            state = State.STOPPED;
            eventsBus.accept(new StoppedEvent(this));
            eventsBus.stop();
        }
    }

    private enum State {
        NEW, STARTED, STOPPED
    }

}