package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class DecodingBuffer {

    private final Utf8TextBuffer textBuffer;
    private final int binaryMessageMaximumSize;
    private final int textMessageMaximumSize;

    public DecodingBuffer(InputStream input, int textMessageMaximumSize, int binaryMessageMaximumSize) {
        this.textBuffer = new Utf8TextBuffer(input);
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    byte[] nextBinaryMessage() throws IOException {
        int maximumSizeOfBase64 = (int) (binaryMessageMaximumSize * 1.34) + 3; // TODO Be more clever here ;)
        String message = textBuffer.nextMessage(maximumSizeOfBase64);
        byte[] decoded = decode(message);
        if (decoded.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = message.substring(0, Math.min(binaryMessageMaximumSize, 256));
            String error = String.format("Incoming binary message \"%s...\" is bigger than allowed %s bytes", encodedMessageFragment, binaryMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return decoded;
    }

    private byte[] decode(String message) throws IOException {
        try {
            return Base64.getDecoder().decode(message);
        } catch (IllegalArgumentException e) {
            throw new IOException("Problem during decoding binary message", e);
        }
    }

    String nextPartialTextMessage() throws IOException {
        return nextTextMessage(textMessageMaximumSize - 1);
    }

    String nextTextMessage() throws IOException {
        return nextTextMessage(textMessageMaximumSize);
    }

    private String nextTextMessage(int maxSize) throws IOException {
        String text = textBuffer.nextMessage(maxSize);
        if (text.length() > maxSize) {
            String error = String.format("Incoming text message \"%s...\" is bigger than allowed %s characters", text.substring(0, maxSize), maxSize);
            throw new StreamsMessageChannelException(error);
        }
        return text;
    }

    char nextChar() throws IOException {
        return textBuffer.nextChar();
    }

}
