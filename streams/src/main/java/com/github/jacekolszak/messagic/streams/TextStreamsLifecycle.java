package com.github.jacekolszak.messagic.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.Lifecycle;
import com.github.jacekolszak.messagic.LifecycleEvent;
import com.github.jacekolszak.messagic.StartedEvent;
import com.github.jacekolszak.messagic.StoppedEvent;

class TextStreamsLifecycle implements Lifecycle {

    private final ChannelDispatchThread dispatchThread;
    private final List<Consumer<StartedEvent>> startedListeners = new ArrayList<>();
    private final List<Consumer<StoppedEvent>> stoppedListeners = new ArrayList<>();

    TextStreamsLifecycle(ChannelDispatchThread dispatchThread) {
        this.dispatchThread = dispatchThread;
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

    void notify(StartedEvent event) {
        for (Consumer<StartedEvent> listener : startedListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

    void notify(StoppedEvent event) {
        for (Consumer<StoppedEvent> listener : stoppedListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

}
