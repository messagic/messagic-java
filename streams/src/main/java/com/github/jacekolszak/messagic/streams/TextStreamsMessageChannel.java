package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.ChannelEvents;
import com.github.jacekolszak.messagic.MessageChannel;

public final class TextStreamsMessageChannel implements MessageChannel {

    private final InputPipe input;
    private final OutputPipe output;
    private final ChannelEventsImpl events;

    private State state = State.NEW;

    public TextStreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public TextStreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        this.events = new ChannelEventsImpl(this);
        this.input = new InputPipe(input, limits, events, exception -> {
            events.notifyError(exception);
            stop();
        });
        this.output = new OutputPipe(output, limits, exception -> {
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