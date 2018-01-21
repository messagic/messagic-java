package com.github.jacekolszak.messagic.streams.events;

import java.util.function.Consumer;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.Error;
import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.EventBus;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.Started;
import com.github.jacekolszak.messagic.Stopped;
import com.github.jacekolszak.messagic.TextMessage;
import com.github.jacekolszak.messagic.streams.input.IncomingMessageListener;

public final class EventBusImpl implements EventBus, IncomingMessageListener {

    private final ChannelDispatchThread dispatchThread;
    private final ListenersSet listenersSet;
    private final MessageChannel channel;

    public EventBusImpl(MessageChannel channel) {
        this.channel = channel;
        this.dispatchThread = new ChannelDispatchThread();
        this.listenersSet = new ListenersSet();
    }

    public void start() {
        dispatchThread.start();
    }

    public void stop() {
        dispatchThread.stop();
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener) {
        listenersSet.add(eventClass, listener);
    }

    @Override
    public <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener) {
        listenersSet.remove(eventClass, listener);
    }

    public void notifyStarted() {
        Started event = () -> channel;
        notify(event);
    }

    private void notify(Started event) {
        listenersSet.listenersOfType(Started.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    public void notifyStopped() {
        Stopped event = () -> channel;
        notify(event);
    }

    private void notify(Stopped event) {
        listenersSet.listenersOfType(Stopped.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    @Override
    public void textMessageFound(String textMessage) {
        notify(new TextMessageEvent(channel, textMessage));
    }

    private void notify(TextMessage event) {
        listenersSet.listenersOfType(TextMessage.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    @Override
    public void binaryMessageFound(byte[] binaryMessage) {
        notify(new BinaryMessageEvent(channel, binaryMessage));
    }

    private void notify(BinaryMessage event) {
        listenersSet.listenersOfType(BinaryMessage.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    public void notifyError(Exception exception) {
        notify(new ErrorEvent(channel, exception));
    }

    private void notify(Error event) {
        listenersSet.listenersOfType(Error.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

}
