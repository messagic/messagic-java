package com.github.messagic.fake;

import java.util.function.Consumer;

import com.github.messagic.Event;
import com.github.messagic.MessageChannel;
import com.github.messagic.fake.eventbus.EventBus;

public final class FakeMessageChannel implements MessageChannel {

    private final EventBus eventBus;
    private FakeMessageChannel connectedChannel;

    public FakeMessageChannel() {
        this.eventBus = new EventBus();
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener) {
        eventBus.addListener(eventClass, listener);
    }

    @Override
    public <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener) {
        eventBus.removeListener(eventClass, listener);
    }

    @Override
    public void start() {
        eventBus.start();
    }

    @Override
    public void send(String textMessage) {
        connectedChannel.eventBus.accept(new TextMessageEvent(connectedChannel, textMessage));
    }

    @Override
    public void send(byte[] binaryMessage) {
        connectedChannel.eventBus.accept(new BinaryMessageEvent(connectedChannel, binaryMessage));
    }

    @Override
    public void stop() {
        eventBus.accept(new StoppedEvent(this));
        connectedChannel.eventBus.accept(new StoppedEvent(connectedChannel));
    }

    public void connect(FakeMessageChannel channel) {
        connected(channel);
        channel.connected(this);
    }

    private void connected(FakeMessageChannel channel) {
        this.connectedChannel = channel;
        eventBus.accept(new StartedEvent(this));
    }

}
