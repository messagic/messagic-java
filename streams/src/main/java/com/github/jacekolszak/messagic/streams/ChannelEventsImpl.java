package com.github.jacekolszak.messagic.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.ChannelEvents;
import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.StartedEvent;
import com.github.jacekolszak.messagic.StoppedEvent;
import com.github.jacekolszak.messagic.TextMessage;

final class ChannelEventsImpl implements ChannelEvents, IncomingMessageListener {

    private final ChannelDispatchThread dispatchThread;
    private final List<Consumer<StartedEvent>> startedListeners = new ArrayList<>();
    private final List<Consumer<StoppedEvent>> stoppedListeners = new ArrayList<>();
    private final List<Consumer<TextMessage>> textMessageListeners = new ArrayList<>();
    private final List<Consumer<BinaryMessage>> binaryMessageListeners = new ArrayList<>();
    private final MessageChannel channel;

    ChannelEventsImpl(MessageChannel channel) {
        this.channel = channel;
        this.dispatchThread = new ChannelDispatchThread();
    }

    void start() {
        dispatchThread.start();
    }

    void stop() {
        dispatchThread.stop();
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener) {
        if (eventClass.equals(StartedEvent.class)) {
            startedListeners.add((Consumer<StartedEvent>) listener);
        } else if (eventClass.equals(StoppedEvent.class)) {
            stoppedListeners.add((Consumer<StoppedEvent>) listener);
        } else if (eventClass.equals(TextMessage.class)) {
            textMessageListeners.add((Consumer<TextMessage>) listener);
        } else if (eventClass.equals(BinaryMessage.class)) {
            binaryMessageListeners.add((Consumer<BinaryMessage>) listener);
        }
    }

    @Override
    public <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener) {
        if (eventClass.equals(StartedEvent.class)) {
            startedListeners.remove(listener);
        } else if (eventClass.equals(StoppedEvent.class)) {
            stoppedListeners.remove(listener);
        } else if (eventClass.equals(TextMessage.class)) {
            textMessageListeners.remove(listener);
        } else if (eventClass.equals(BinaryMessage.class)) {
            binaryMessageListeners.remove(listener);
        }
    }

    void notifyStarted() {
        StartedEvent event = () -> channel;
        notify(event);
    }

    private void notify(StartedEvent event) {
        for (Consumer<StartedEvent> listener : startedListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

    void notifyStopped() {
        StoppedEvent event = () -> channel;
        notify(event);
    }

    private void notify(StoppedEvent event) {
        for (Consumer<StoppedEvent> listener : stoppedListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

    @Override
    public void textMessageFound(String textMessage) {
        TextMessage event = new TextMessage() {
            @Override
            public MessageChannel channel() {
                return channel;
            }

            @Override
            public String text() {
                return textMessage;
            }
        };
        notify(event);
    }

    private void notify(TextMessage event) {
        for (Consumer<TextMessage> listener : textMessageListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

    @Override
    public void binaryMessageFound(byte[] binaryMessage) {
        BinaryMessage event = new BinaryMessage() {
            @Override
            public MessageChannel channel() {
                return channel;
            }

            @Override
            public byte[] bytes() {
                return binaryMessage;
            }
        };
        notify(event);
    }

    private void notify(BinaryMessage event) {
        for (Consumer<BinaryMessage> listener : binaryMessageListeners) {
            dispatchThread.execute(() -> listener.accept(event));
        }
    }

}
