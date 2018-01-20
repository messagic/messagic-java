package com.github.jacekolszak.messagic.streams;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.Event;

class ListenersSet {

    private Map<Class<? extends Event>, Set<Consumer<? extends Event>>> map = new HashMap<>();

    public void add(Class<? extends Event> eventClass, Consumer<? extends Event> listener) {
        if (!map.containsKey(eventClass)) {
            map.put(eventClass, new LinkedHashSet<>());
        }
        map.get(eventClass).add(listener);
    }

    public void remove(Class<? extends Event> eventClass, Consumer<? extends Event> listener) {
        Set<Consumer<? extends Event>> consumers = map.get(eventClass);
        if (consumers != null) {
            consumers.remove(listener);
        }
    }

    public <T extends Event> Set<Consumer<T>> listenersOfType(Class<T> eventClass) {
        return (Set) map.getOrDefault(eventClass, Set.of());
    }

}
