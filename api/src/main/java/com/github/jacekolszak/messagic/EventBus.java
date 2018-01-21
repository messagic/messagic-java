package com.github.jacekolszak.messagic;

import java.util.function.Consumer;

public interface EventBus {

    <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener);

    <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener);

}
