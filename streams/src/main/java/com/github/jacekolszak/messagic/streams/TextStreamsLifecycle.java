package com.github.jacekolszak.messagic.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.jacekolszak.messagic.Lifecycle;
import com.github.jacekolszak.messagic.LifecycleEvent;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.StartedEvent;
import com.github.jacekolszak.messagic.StoppedEvent;

class TextStreamsLifecycle implements Lifecycle, LifecycleUpdater {

    private final List<Consumer<StartedEvent>> startedListeners = new ArrayList<>();
    private final List<Consumer<StoppedEvent>> stoppedListeners = new ArrayList<>();
    private final Logger logger = Logger.getLogger(TextStreamsLifecycle.class.getName());
    private final MessageChannel channel;
    private final Runnable onStart;
    private final Runnable onStop;
    private State state = State.NEW;

    TextStreamsLifecycle(MessageChannel channel, Runnable onStart, Runnable onStop) {
        this.channel = channel;
        this.onStart = onStart;
        this.onStop = onStop;
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
    public <T extends LifecycleEvent> void removeEventListener(Consumer<T> listener) {

    }

    @Override
    public void start() {
        if (state == State.NEW) {
            onStart.run();
            state = State.STARTED;
            StartedEvent event = () -> channel;
            notify(event);
        } else if (state == State.STOPPED) {
            throw new IllegalStateException("Cant start channel which was stopped before");
        }
    }

    private void notify(StartedEvent event) {
        for (Consumer<StartedEvent> listener : startedListeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Listener thrown exception", e);
            }
        }
    }

    @Override
    public void stop() {
        if (state == State.STARTED) {
            onStop.run();
            state = State.STOPPED;
            StoppedEvent event = () -> channel;
            notify(event);
        }
    }

    private void notify(StoppedEvent event) {
        for (Consumer<StoppedEvent> listener : stoppedListeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Listener thrown exception", e);
            }
        }
    }

    private enum State {
        NEW, STARTED, STOPPED
    }

}
