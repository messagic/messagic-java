package com.github.messagic.streams.input;

import java.io.IOException;
import java.util.Base64;

import com.github.messagic.MessageChannel;
import com.github.messagic.streams.StreamsMessageChannelException;

final class BinaryMessage implements Message {

    private final String encodedMessage;
    private final MessageChannel channel;
    private final int binaryMessageMaximumSize;

    BinaryMessage(MessageChannel channel, String encodedMessage, int binaryMessageMaximumSize) {
        this.channel = channel;
        this.encodedMessage = encodedMessage;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
    }

    @Override
    public DecodedMessage decodedMessage() throws IOException {
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(encodedMessage);
        } catch (IllegalArgumentException e) {
            throw new StreamsMessageChannelException("Problem during decoding binary message", e);
        }
        if (decodedBytes.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = encodedMessage.substring(0, Math.min(binaryMessageMaximumSize, 256));
            String error = String.format("Incoming binary message \"%s...\" is bigger than allowed %s bytes",
                    encodedMessageFragment, binaryMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return () -> new BinaryMessageEvent(channel, decodedBytes);
    }

}
