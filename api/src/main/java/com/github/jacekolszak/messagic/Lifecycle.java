package com.github.jacekolszak.messagic;

import java.util.function.Consumer;

/**
 * Channel has following states: NEW -> STARTED -> STOPPED
 */
public interface Lifecycle {

    <T extends LifecycleEvent> void addEventListener(Class<T> eventClass, Consumer<T> listener);

    <T extends LifecycleEvent> void removeEventListener(Class<T> eventClass, Consumer<T> listener);

}
