package com.github.jacekolszak.messagic.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.IncomingStream;

class TextStreamsIncomingStream implements IncomingStream, MessagePublisher {

    private final List<Consumer<String>> textMessageListeners = new ArrayList<>();
    private final List<Consumer<byte[]>> binaryMessageListeners = new ArrayList<>();
    private final ChannelDispatchThread dispatchThread;

    TextStreamsIncomingStream(ChannelDispatchThread dispatchThread) {
        this.dispatchThread = dispatchThread;
    }

    @Override
    public void addTextMessageListener(Consumer<String> listener) {
        textMessageListeners.add(listener);
    }

    @Override
    public void removeTextMessageListener(Consumer<String> listener) {
        textMessageListeners.remove(listener);
    }

    @Override
    public void publish(String textMessage) {
        for (Consumer<String> listener : textMessageListeners) {
            dispatchThread.execute(() -> listener.accept(textMessage));
        }
    }

    @Override
    public void addBinaryMessageListener(Consumer<byte[]> listener) {
        binaryMessageListeners.add(listener);
    }

    @Override
    public void removeBinaryMessageListener(Consumer<byte[]> listener) {
        binaryMessageListeners.remove(listener);
    }

    @Override
    public void publish(byte[] binaryMessage) {
        for (Consumer<byte[]> listener : binaryMessageListeners) {
            dispatchThread.execute(() -> listener.accept(binaryMessage));
        }
    }
}
