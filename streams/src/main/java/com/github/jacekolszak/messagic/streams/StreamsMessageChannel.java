package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.EventBus;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.eventbus.EventBusImpl;
import com.github.jacekolszak.messagic.streams.input.DecodingBuffer;
import com.github.jacekolszak.messagic.streams.input.InputPipeThread;
import com.github.jacekolszak.messagic.streams.input.MessageEventsStream;
import com.github.jacekolszak.messagic.streams.output.MessageFactory;
import com.github.jacekolszak.messagic.streams.output.OutputPipe;

/**
 * MessageChannel implementation using Input and OutputStream. Protocol is text-based in UTF-8 encoding. Binary messages are encoded using Base64. Every message is separated using new line "\n".
 */
public final class StreamsMessageChannel implements MessageChannel {

    private final InputPipeThread inputPipeThread;
    private final OutputPipe outputPipe;
    private final EventBusImpl events;

    private State state = State.NEW;

    public StreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public StreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        events = new EventBusImpl();
        DecodingBuffer decodingBuffer = limits.decodingBuffer(input);
        MessageEventsStream messageEventsStream = new MessageEventsStream(decodingBuffer, this);
        inputPipeThread = new InputPipeThread(messageEventsStream, events, exception -> {
            events.accept(new ErrorEvent(this, exception));
            stop();
        });
        MessageFactory messageFactory = limits.messageFactory();
        outputPipe = new OutputPipe(output, messageFactory, exception -> {
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
            inputPipeThread.start();
            state = State.STARTED;
            events.start();
            events.accept(new StartedEvent(this));
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
    public void stop() {
        if (state == State.STARTED) {
            try {
                this.inputPipeThread.stop();
            } finally {
                this.outputPipe.stop();
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