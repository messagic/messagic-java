package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.EventBus;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.eventbus.EventBusImpl;
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
    private final EventBusImpl events;

    private State state = State.NEW;

    public StreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public StreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        this.events = new EventBusImpl();
        MessageStream messageStream = limits.messageStream(input, this);
        this.input = new InputPipe(messageStream, events, exception -> {
            events.accept(new ErrorEvent(this, exception));
            stop();
        });
        MessageFactory messageFactory = limits.messageFactory();
        this.output = new OutputPipe(output, messageFactory, exception -> {
            events.accept(new ErrorEvent(this, exception));
            stop();
        });
    }

    @Override
    public EventBus eventBus() {
        return events;
    }

    @Override
    public void start() {
        if (state == State.NEW) {
            input.start();
            state = State.STARTED;
            events.start();
            events.accept(new StartedEvent(this));
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
            events.accept(new StoppedEvent(this));
            events.stop();
        }
    }

    private enum State {
        NEW, STARTED, STOPPED
    }

}