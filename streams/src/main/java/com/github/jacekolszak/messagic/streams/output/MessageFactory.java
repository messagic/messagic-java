package com.github.jacekolszak.messagic.streams.output;

import java.util.Arrays;
import java.util.Base64;

import com.github.jacekolszak.messagic.streams.TextStreamsException;

public final class MessageFactory {

    private final int textMessageMaximumSize;
    private final int binaryMessageMaximumSize;

    public MessageFactory(int textMessageMaximumSize, int binaryMessageMaximumSize) {
        this.textMessageMaximumSize = textMessageMaximumSize;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
    }

    TextMessage textMessage(String text) throws TextStreamsException {
        if (text.length() > textMessageMaximumSize) {
            String error = String.format("Outgoing text message \"%s...\" is bigger than allowed %s characters", text.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new TextStreamsException(error);
        }
        return new TextMessage(text);
    }

    BinaryMessage binaryMessage(byte[] bytes) throws TextStreamsException {
        if (bytes.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = Base64.getEncoder().encodeToString(Arrays.copyOfRange(bytes, 0, binaryMessageMaximumSize));
            String error = String.format("Outgoing binary message \"%s...\" is bigger than allowed %s bytes", encodedMessageFragment, binaryMessageMaximumSize);
            throw new TextStreamsException(error);
        }
        return new BinaryMessage(bytes);
    }

}
