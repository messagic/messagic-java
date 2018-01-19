package com.github.jacekolszak.messagic.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.jacekolszak.messagic.MessageListeners;

class TextStreamsMessageListeners implements MessageListeners, MessagePublisher {

    private static final Logger logger = Logger.getLogger(TextStreamsMessageListeners.class.getName());
    private final List<Consumer<String>> textMessageListeners = new ArrayList<>();
    private final List<Consumer<byte[]>> binaryMessageListeners = new ArrayList<>();

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
            try {
                listener.accept(textMessage);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Listener thrown exception", e);
            }
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
            try {
                listener.accept(binaryMessage);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Listener thrown exception", e);
            }
        }
    }
}
