package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.IncomingStream;
import com.github.jacekolszak.messagic.Lifecycle;
import com.github.jacekolszak.messagic.LifecycleEvent;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.StartedEvent;
import com.github.jacekolszak.messagic.StoppedEvent;

public class TextStreamsMessageChannel implements MessageChannel, Lifecycle {

    private final InputPipe input;
    private final OutputPipe output;
    private final TextStreamsIncomingStream messageConsumers;
    private final List<Consumer<StartedEvent>> startedListeners = new ArrayList<>();
    private final List<Consumer<StoppedEvent>> stoppedListeners = new ArrayList<>();
    private final ChannelDispatchThread dispatchThread;

    private State state = State.NEW;

    public TextStreamsMessageChannel(InputStream input, OutputStream output) {
        this.dispatchThread = new ChannelDispatchThread();
        this.messageConsumers = new TextStreamsIncomingStream(dispatchThread);
        this.input = new InputPipe(input, messageConsumers);
        this.output = new OutputPipe(output);
    }

    @Override
    public Lifecycle lifecycle() {
        return this;
    }

    @Override
    public IncomingStream incomingStream() {
        return messageConsumers;
    }

    @Override
    public void start() {
        start(new Limits());
    }

    public void start(Limits limits) {
        if (state == State.NEW) {
            dispatchThread.start();
            input.start();
            state = State.STARTED;
            StartedEvent event = () -> this;
            notify(event);
        } else if (state == State.STOPPED) {
            throw new IllegalStateException("Cant start channel which was stopped before");
        }
    }

    private void notify(StartedEvent event) {
        for (Consumer<StartedEvent> listener : startedListeners) {
            dispatchThread.execute(() -> listener.accept(event));
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
            notify(event);
            dispatchThread.stop();
        }
    }

    private void notify(StoppedEvent event) {
        for (Consumer<StoppedEvent> listener : stoppedListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

    @Override
    public <T extends LifecycleEvent> void addEventListener(Class<T> eventClass, Consumer<T> listener) {
        if (eventClass.equals(StartedEvent.class)) {
            startedListeners.add((Consumer<StartedEvent>) listener);
        } else if (eventClass.equals(StoppedEvent.class)) {
            stoppedListeners.add((Consumer<StoppedEvent>) listener);
        }
    }

    @Override
    public <T extends LifecycleEvent> void removeEventListener(Class<T> eventClass, Consumer<T> listener) {
        if (eventClass.equals(StartedEvent.class)) {
            startedListeners.remove(listener);
        } else if (eventClass.equals(StoppedEvent.class)) {
            stoppedListeners.remove(listener);
        }
    }

    private enum State {
        NEW, STARTED, STOPPED
    }

}