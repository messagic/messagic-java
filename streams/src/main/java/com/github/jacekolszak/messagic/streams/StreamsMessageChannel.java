package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.ChannelEvents;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.events.ChannelEventsImpl;
import com.github.jacekolszak.messagic.streams.input.InputPipe;
import com.github.jacekolszak.messagic.streams.input.MessageStream;
import com.github.jacekolszak.messagic.streams.output.MessageFactory;
import com.github.jacekolszak.messagic.streams.output.OutputPipe;

/**
 * MessageChannel implementation using Input and OutputStream. Protocol is text-based, encoding binary messages using Base64.
 */
public final class StreamsMessageChannel implements MessageChannel {

    private final InputPipe input;
    private final OutputPipe output;
    private final ChannelEventsImpl events;

    private State state = State.NEW;

    public StreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public StreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        this.events = new ChannelEventsImpl(this);
        MessageStream messageStream = limits.messageStream(input, events);
        this.input = new InputPipe(messageStream, exception -> {
            events.notifyError(exception);
            stop();
        });
        MessageFactory messageFactory = limits.messageFactory();
        this.output = new OutputPipe(output, messageFactory, exception -> {
            events.notifyError(exception);
            stop();
        });
    }

    @Override
    public ChannelEvents events() {
        return events;
    }

    @Override
    public void start() {
        if (state == State.NEW) {
            input.start();
            state = State.STARTED;
            events.start();
            events.notifyStarted();
        } else if (state == State.STOPPED) {
            throw new IllegalStateException("Can't start channel which was stopped before");
        }
    }

    @Override
    public void send(String textMessage) {
        output.send(textMessage);
    }

    @Override
    public void send(byte[] binaryMessage) {
        output.send(binaryMessage);
    }

    @Override
    public void stop() {
        if (state == State.STARTED) {
            try {
                this.input.stop();
            } finally {
                this.output.stop();
            }
            state = State.STOPPED;
            events.notifyStopped();
            events.stop();
        }
    }

    private enum State {
        NEW, STARTED, STOPPED
    }

}