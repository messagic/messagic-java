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
package com.github.messagic.fake.eventbus;

import java.util.Set;
import java.util.function.Consumer;

import com.github.messagic.Event;

public final class EventBus implements Consumer<Event> {

    private final ChannelDispatchThread dispatchThread;
    private final ListenersSet listenersSet;

    public EventBus() {
        this.dispatchThread = new ChannelDispatchThread();
        this.listenersSet = new ListenersSet();
    }

    public void start() {
        dispatchThread.start();
    }

    public void stop() {
        dispatchThread.stop();
    }

    public <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener) {
        try {
            listenersSet.add(eventClass, listener);
        } catch (RuntimeException e) {
            throw new RuntimeException("Can't add listener", e);
        }
    }

    public <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener) {
        try {
            listenersSet.remove(eventClass, listener);
        } catch (RuntimeException e) {
            throw new RuntimeException("Can't remove listener", e);
        }
    }

    @Override
    public void accept(Event event) {
        Set<Consumer<Event>> listeners = listenersSet.listenersForEvent(event);
        listeners.forEach(listener ->
                dispatchThread.execute(() -> listener.accept(event))
        );
    }

}
