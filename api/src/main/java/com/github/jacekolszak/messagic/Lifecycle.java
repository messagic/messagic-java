package com.github.jacekolszak.messagic;

import java.util.function.Consumer;

public interface Lifecycle {

    <T extends LifecycleEvent> void addEventListener(Class<T> eventClass, Consumer<T> listener);

    <T extends LifecycleEvent> void removeEventListener(Consumer<T> listener);

}
