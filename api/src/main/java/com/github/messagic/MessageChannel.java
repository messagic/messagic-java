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
