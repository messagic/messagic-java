package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.IncomingStream;
import com.github.jacekolszak.messagic.Lifecycle;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.StartedEvent;
import com.github.jacekolszak.messagic.StoppedEvent;

public final class TextStreamsMessageChannel implements MessageChannel {

    private final InputPipe input;
    private final OutputPipe output;
    private final TextStreamsIncomingStream incomingStream;
    private final ChannelDispatchThread dispatchThread;
    private final TextStreamsLifecycle lifecycle;

    private State state = State.NEW;

    public TextStreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public TextStreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        this.dispatchThread = new ChannelDispatchThread();
        this.incomingStream = new TextStreamsIncomingStream(dispatchThread);
        this.input = new InputPipe(input, limits, incomingStream, this::stop);
        this.output = new OutputPipe(output, limits, this::stop);
        this.lifecycle = new TextStreamsLifecycle(dispatchThread);
    }

    @Override
    public Lifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public IncomingStream incomingStream() {
        return incomingStream;
    }

    @Override
    public void start() {
        if (state == State.NEW) {
            dispatchThread.start();
            input.start();
            state = State.STARTED;
            StartedEvent event = () -> this;
            lifecycle.notify(event);
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
            StoppedEvent event = () -> this;
            lifecycle.notify(event);
            dispatchThread.stop();
        }
    }

    private enum State {
        NEW, STARTED, STOPPED
    }

}