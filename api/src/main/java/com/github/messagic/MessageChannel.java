package com.github.messagic;

import java.util.function.Consumer;

/**
 * Sending messages or starting and stopping the channel is asynchronous, ie. does not block the current thread until it is
 * done.
 * <p>
 * All message and events listeners are run asynchronously in a sequential manner (depending on the order they were
 * registered)
 * <p>
 * All implementation classes must be thread-safe
 */
public interface MessageChannel {

    <T extends Event> void addListener(Class<T> eventClass, Consumer<T> listener);

    <T extends Event> void removeListener(Class<T> eventClass, Consumer<T> listener);

    /**
     * Once started channel allows to send messages and accepts incoming ones.
     * <p>
     * After successful start "Started" event is published.
     * <p>
     * Running this method when MessageChannel is already started has no effect.
     * <p>
     * Running this method after channel was stopped will throw IllegalStateException
     */
    void start();

    void send(String textMessage);

    void send(byte[] binaryMessage);

    /**
     * Once stopped no new messages can be send and no incoming messages will be read.
     * <p>
     * Implementation may close the socket, stream or whatever.
     * <p>
     * After successful stop "Stopped" event is published.
     * <p>
     * Running this method when MessageChannel is already stopped or not yet started has no effect.
     */
    void stop();

}
