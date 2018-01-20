package com.github.jacekolszak.messagic.streams.events;

import java.util.function.Consumer;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.ChannelEvents;
import com.github.jacekolszak.messagic.Error;
import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.Started;
import com.github.jacekolszak.messagic.Stopped;
import com.github.jacekolszak.messagic.TextMessage;

public final class ChannelEventsImpl implements ChannelEvents, IncomingMessageListener {

    private final ChannelDispatchThread dispatchThread;
    private final ListenersSet listenersSet;
    private final MessageChannel channel;

    public ChannelEventsImpl(MessageChannel channel) {
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
        TextMessage event = new TextMessageImpl(textMessage);
        notify(event);
    }

    private void notify(TextMessage event) {
        listenersSet.listenersOfType(TextMessage.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    @Override
    public void binaryMessageFound(byte[] binaryMessage) {
        BinaryMessage event = new BinaryMessageImpl(binaryMessage);
        notify(event);
    }

    private void notify(BinaryMessage event) {
        listenersSet.listenersOfType(BinaryMessage.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    public void notifyError(Exception exception) {
        Error event = new ErrorImpl(exception);
        notify(event);
    }

    private void notify(Error event) {
        listenersSet.listenersOfType(Error.class)
                .forEach(listener ->
                        dispatchThread.execute(() -> listener.accept(event))
                );
    }

    private class TextMessageImpl implements TextMessage {

        private final String textMessage;

        TextMessageImpl(String textMessage) {
            this.textMessage = textMessage;
        }

        @Override
        public MessageChannel channel() {
            return channel;
        }

        @Override
        public String text() {
            return textMessage;
        }
    }

    private class BinaryMessageImpl implements BinaryMessage {

        private final byte[] binaryMessage;

        BinaryMessageImpl(byte[] binaryMessage) {
            this.binaryMessage = binaryMessage;
        }

        @Override
        public MessageChannel channel() {
            return channel;
        }

        @Override
        public byte[] bytes() {
            return binaryMessage;

        }
    }

    private class ErrorImpl implements Error {

        private final Exception exception;

        ErrorImpl(Exception exception) {
            this.exception = exception;
        }

        @Override
        public MessageChannel channel() {
            return channel;
        }

        @Override
        public Exception exception() {
            return exception;
        }

    }
}
