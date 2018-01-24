package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class MessageEventsStream {

    private final MessageStream messageStream;
    private final int textMessageMaximumSize;
    private final int binaryMessageMaximumSize;
    private final MessageChannel channel;

    private Event event;

    public MessageEventsStream(InputStream input, int textMessageMaximumSize, int binaryMessageMaximumSize,
                               MessageChannel channel) {
        this.messageStream = new MessageStream(input);
        this.textMessageMaximumSize = textMessageMaximumSize;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.channel = channel;
    }

    void moveToNextEvent() throws IOException {
        int maximumSizeOfBase64 = (int) (binaryMessageMaximumSize * 1.34) + 3; // TODO Be more clever here ;)
        messageStream.readMessage(textMessageMaximumSize, maximumSizeOfBase64);
        String message = messageStream.message();
        if (messageStream.binaryMessage()) {
            event = binaryMessageEvent(message);
        } else {
            event = textMessageEvent(message);
        }
    }

    Event event() {
        return event;
    }

    private Event binaryMessageEvent(String message) throws IOException {
        byte[] decoded = decode(message);
        if (decoded.length > binaryMessageMaximumSize) {
            String encodedMessageFragment = message.substring(0, Math.min(binaryMessageMaximumSize, 256));
            String error = String.format("Incoming binary message \"%s...\" is bigger than allowed %s bytes",
                    encodedMessageFragment, binaryMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return new BinaryMessageEvent(channel, decoded);
    }

    private Event textMessageEvent(String message) throws StreamsMessageChannelException {
        if (message.length() > textMessageMaximumSize) {
            String error = String.format("Incoming text message \"%s...\" is bigger than allowed %s characters",
                    message.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return new TextMessageEvent(channel, message);
    }

    private byte[] decode(String message) throws IOException {
        try {
            return Base64.getDecoder().decode(message);
        } catch (IllegalArgumentException e) {
            throw new IOException("Problem during decoding binary message", e);
        }
    }

}
