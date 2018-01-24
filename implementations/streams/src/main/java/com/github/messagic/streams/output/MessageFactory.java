package com.github.messagic.streams.output;

import java.util.Arrays;
import java.util.Base64;

import com.github.messagic.streams.StreamsMessageChannelException;

public final class MessageFactory {

    private final int textMessageMaximumSize;
    private final int binaryMessageMaximumSize;

    public MessageFactory(int textMessageMaximumSize, int binaryMessageMaximumSize) {
        this.textMessageMaximumSize = textMessageMaximumSize;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
    }

    TextMessage textMessage(String text) throws StreamsMessageChannelException {
        if (text.length() > textMessageMaximumSize) {
            String error = String.format("Outgoing text message \"%s...\" is bigger than allowed %s characters",
                    text.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        if (text.contains("\n")) {
            return new MultiLineTextMessage(text);
        } else {
            return new OneLineTextMessage(text);
        }
    }

    BinaryMessage binaryMessage(byte[] bytes) throws StreamsMessageChannelException {
        if (bytes.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = Base64.getEncoder().encodeToString(
                    Arrays.copyOfRange(bytes, 0, Math.min(binaryMessageMaximumSize, 256)));
            String error = String.format("Outgoing binary message \"%s...\" is bigger than allowed %s bytes",
                    encodedMessageFragment, binaryMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return new BinaryMessage(bytes);
    }

}
