/*
 * Copyright 2018 The Messagic Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.messagic.fake;

import java.util.function.Consumer;

import com.github.messagic.Event;
import com.github.messagic.MessageChannel;
import com.github.messagic.fake.eventbus.EventBus;

public final class FakeMessageChannel implements MessageChannel {

    private final EventBus eventBus;
    private FakeMessageChannel connectedChannel;
    private boolean started;

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
        started = true;
        if (connectedChannel != null) {
            connect(connectedChannel); // TODO This works but looks like #!@
        }
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
        if (connectedChannel != null) {
            eventBus.accept(new StoppedEvent(this));
            connectedChannel.eventBus.accept(new StoppedEvent(connectedChannel));
        }
    }

    public void connect(FakeMessageChannel channel) {
        connected(channel);
        channel.connected(this);
    }

    private void connected(FakeMessageChannel channel) {
        this.connectedChannel = channel;
        if (started) {
            eventBus.accept(new StartedEvent(this));
        }
    }

}
