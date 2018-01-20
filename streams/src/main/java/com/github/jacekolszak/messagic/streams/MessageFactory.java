package com.github.jacekolszak.messagic.streams;

import java.util.Arrays;
import java.util.Base64;

final class MessageFactory {

    private final Limits limits;

    MessageFactory(Limits limits) {
        this.limits = limits;
    }

    TextMessage textMessage(String text) throws TextStreamsException {
        if (text.length() > limits.textMessageMaximumSize) {
            String error = String.format("Outgoing text message \"%s...\" is bigger than allowed %s characters", text.substring(0, limits.textMessageMaximumSize), limits.textMessageMaximumSize);
            throw new TextStreamsException(error);
        }
        return new TextMessage(text);
    }

    BinaryMessage binaryMessage(byte[] bytes) throws TextStreamsException {
        if (bytes.length > limits.binaryMessageMaximumSize) {
            String encodedMessageFragment = Base64.getEncoder().encodeToString(Arrays.copyOfRange(bytes, 0, limits.binaryMessageMaximumSize));
            String error = String.format("Outgoing binary message \"%s...\" is bigger than allowed %s bytes", encodedMessageFragment, limits.binaryMessageMaximumSize);
            throw new TextStreamsException(error);
        }
        return new BinaryMessage(bytes);
    }

}
