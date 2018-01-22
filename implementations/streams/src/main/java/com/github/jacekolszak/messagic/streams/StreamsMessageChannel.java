package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.eventbus.EventBus;
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
    private final EventBus eventsBus;

    private State state = State.NEW;

    public StreamsMessageChannel(InputStream input, OutputStream output) {
        this(input, output, new Limits());
    }

    public StreamsMessageChannel(InputStream input, OutputStream output, Limits limits) {
        eventsBus = new EventBus();
        DecodingBuffer decodingBuffer = limits.decodingBuffer(input);
        MessageEventsStream messageEventsStream = new MessageEventsStream(decodingBuffer, this);
        inputPipeThread = new InputPipeThread(messageEventsStream, eventsBus, exception -> {
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