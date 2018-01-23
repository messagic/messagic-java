package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.TextMessage;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class MessageEventsStream {

    private final Utf8TextBuffer textBuffer;
    private final int textMessageMaximumSize;
    private final int binaryMessageMaximumSize;
    private final MessageChannel channel;

    private Event event;

    public MessageEventsStream(InputStream input, int textMessageMaximumSize, int binaryMessageMaximumSize,
                               MessageChannel channel) {
        this.textBuffer = new Utf8TextBuffer(input);
        this.textMessageMaximumSize = textMessageMaximumSize;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.channel = channel;
    }

    void moveToNextEvent() throws IOException {
        char typeOrFistCharacter = textBuffer.nextChar();
        switch (typeOrFistCharacter) {
            case '$':
                event = binaryMessageEvent(nextBinaryMessage());
                break;
            case '\n':
                event = textMessageEvent("");
                break;
            case '#':
                event = textMessageEvent(textBuffer.nextMessage(textMessageMaximumSize));
                break;
            case '@':
                event = textMessageEvent(textBuffer.nextMultilineMessage(textMessageMaximumSize));
                break;
            default:
                event = textMessageEvent(typeOrFistCharacter + textBuffer.nextMessage(textMessageMaximumSize - 1));
        }
    }

    Event event() {
        return event;
    }

    private BinaryMessage binaryMessageEvent(byte[] message) {
        return new BinaryMessageEvent(channel, message);
    }

    private TextMessage textMessageEvent(String text) throws StreamsMessageChannelException {
        if (text.length() > textMessageMaximumSize) {
            String error = String.format("Incoming text message \"%s...\" is bigger than allowed %s characters",
                    text.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return new TextMessageEvent(channel, text);
    }

    private byte[] nextBinaryMessage() throws IOException {
        int maximumSizeOfBase64 = (int) (binaryMessageMaximumSize * 1.34) + 3; // TODO Be more clever here ;)
        String message = textBuffer.nextMessage(maximumSizeOfBase64);
        byte[] decoded = decode(message);
        if (decoded.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = message.substring(0, Math.min(binaryMessageMaximumSize, 256));
            String error = String.format("Incoming binary message \"%s...\" is bigger than allowed %s bytes",
                    encodedMessageFragment, binaryMessageMaximumSize);
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

}
