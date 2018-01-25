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
package com.github.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.github.messagic.streams.StreamsMessageChannelException;

public final class OutputPipe {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream output;
    private final MessageFactory messageFactory;
    private final Consumer<Exception> onError;

    private volatile boolean stopped;

    public OutputPipe(OutputStream output, MessageFactory messageFactory, Consumer<Exception> onError) {
        this.output = output;
        this.messageFactory = messageFactory;
        this.onError = onError;
    }

    public void send(String textMessage) {
        if (!stopped) {
            executor.submit(() -> {
                try {
                    TextMessage message = messageFactory.textMessage(textMessage);
                    message.encode(output);
                } catch (IOException e) {
                    onError.accept(new StreamsMessageChannelException("Problem sending text message", e));
                }
            });
        }
    }

    public void send(byte[] binaryMessage) {
        if (!stopped) {
            executor.submit(() -> {
                try {
                    BinaryMessage message = messageFactory.binaryMessage(binaryMessage);
                    message.encode(output);
                } catch (IOException e) {
                    onError.accept(new StreamsMessageChannelException("Problem sending binary message", e));
                }
            });
        }
    }

    public synchronized void stop() {
        stopped = true;
        executor.shutdown();
    }

}
