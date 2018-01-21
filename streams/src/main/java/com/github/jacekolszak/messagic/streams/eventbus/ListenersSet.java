package com.github.jacekolszak.messagic.streams.eventbus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.Error;
import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.Started;
import com.github.jacekolszak.messagic.Stopped;
import com.github.jacekolszak.messagic.TextMessage;

final class ListenersSet {

    private Map<EventType, Set<Consumer<? extends Event>>> map = new HashMap<>();

    void add(Class<? extends Event> eventClass, Consumer<? extends Event> listener) {
        EventType eventType = EventType.fromEventClass(eventClass);
        if (!map.containsKey(eventType)) {
            map.put(eventType, new LinkedHashSet<>());
        }
        map.get(eventType).add(listener);
    }

    void remove(Class<? extends Event> eventClass, Consumer<? extends Event> listener) {
        EventType eventType = EventType.fromEventClass(eventClass);
        Set<Consumer<? extends Event>> consumers = map.get(eventType);
        if (consumers != null) {
            consumers.remove(listener);
        }
    }

    Set<Consumer<Event>> listenersForEventClass(Class<? extends Event> eventClass) {
        EventType eventType = EventType.fromEventClass(eventClass);
        return (Set) map.getOrDefault(eventType, Set.of());
    }

    private enum EventType {

        STARTED(Started.class), STOPPED(Stopped.class), BINARY_MESSAGE(BinaryMessage.class), TEXT_MESSAGE(TextMessage.class), ERROR(Error.class);

        private final Class<? extends Event> eventClass;

        EventType(Class<? extends Event> eventClass) {
            this.eventClass = eventClass;
        }

        static EventType fromEventClass(Class<? extends Event> eventClass) {
            return Arrays.stream(values())
                    .filter(type -> type.eventClass.isAssignableFrom(eventClass))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Event class " + eventClass.getName() + " not supported"));
        }
    }

}
