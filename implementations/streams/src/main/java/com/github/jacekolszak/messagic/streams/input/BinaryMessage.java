package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.util.Base64;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

final class BinaryMessage implements Message {

    private String message;
    private MessageChannel channel;
    private byte[] decodedBytes;
    private int binaryMessageMaximumSize;

    BinaryMessage(MessageChannel channel, String message, int binaryMessageMaximumSize) {
        this.channel = channel;
        this.message = message;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
    }

    @Override
    public void decode() throws IOException {
        try {
            decodedBytes = Base64.getDecoder().decode(message);
        } catch (IllegalArgumentException e) {
            throw new IOException("Problem during decoding binary message", e);
        }
        if (decodedBytes.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = message.substring(0, Math.min(binaryMessageMaximumSize, 256));
            String error = String.format("Incoming binary message \"%s...\" is bigger than allowed %s bytes",
                    encodedMessageFragment, binaryMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
    }

    @Override
    public Event event() {
        return new BinaryMessageEvent(channel, decodedBytes);
    }

}
