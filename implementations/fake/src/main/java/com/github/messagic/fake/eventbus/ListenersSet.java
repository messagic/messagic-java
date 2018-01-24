package com.github.messagic.fake.eventbus;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import com.github.messagic.BinaryMessage;
import com.github.messagic.Error;
import com.github.messagic.Event;
import com.github.messagic.Started;
import com.github.messagic.Stopped;
import com.github.messagic.TextMessage;

final class ListenersSet {

    private final Map<EventType, Set<Consumer<? extends Event>>> map;

    ListenersSet() {
        map = new HashMap<>();
        // preemptively fill map to avoid concurrency issues later
        for (EventType eventType : EventType.values()) {
            map.put(eventType, new CopyOnWriteArraySet<>());
        }
    }

    void add(Class<? extends Event> eventClass, Consumer<? extends Event> listener) {
        EventType eventType = EventType.fromEventClass(eventClass);
        map.get(eventType).add(listener);
    }

    void remove(Class<? extends Event> eventClass, Consumer<? extends Event> listener) {
        EventType eventType = EventType.fromEventClass(eventClass);
        map.get(eventType).remove(listener);
    }

    Set<Consumer<Event>> listenersForEvent(Event event) {
        EventType eventType = EventType.fromEventClass(event.getClass());
        return Collections.unmodifiableSet((Set) map.get(eventType));
    }

    private enum EventType {

        STARTED(Started.class), STOPPED(Stopped.class), BINARY_MESSAGE(BinaryMessage.class),
        TEXT_MESSAGE(TextMessage.class), ERROR(Error.class);

        private final Class<? extends Event> eventClass;

        EventType(Class<? extends Event> eventClass) {
            this.eventClass = eventClass;
        }

        static EventType fromEventClass(Class<? extends Event> eventClass) {
            return Arrays.stream(values())
                    .filter(type -> type.eventClass.isAssignableFrom(eventClass))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Event class " + eventClass.getName() + " not supported"));
        }
    }

}
