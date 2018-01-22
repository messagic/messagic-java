package com.github.jacekolszak.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class OutputPipe {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream output;
    private final MessageFactory messageFactory;
    private final Consumer<Exception> onError;

    private boolean stopped;

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
