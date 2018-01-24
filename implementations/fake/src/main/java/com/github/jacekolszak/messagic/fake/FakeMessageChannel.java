package com.github.jacekolszak.messagic.fake;

import java.util.function.Consumer;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;

final class FakeMessageChannel implements MessageChannel {

    @Override
    public <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener) {

    }

    @Override
    public <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener) {

    }

    @Override
    public void start() {

    }

    @Override
    public void send(String textMessage) {

    }

    @Override
    public void send(byte[] binaryMessage) {

    }

    @Override
    public void stop() {

    }

    public void connect(FakeMessageChannel channel) {
    }

}
