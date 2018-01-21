package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;

public final class MessageStream {

    private final LimitedBuffer buffer;
    private final MessageChannel channel;

    public MessageStream(InputStream input, MessageChannel channel, int textMessageMaximumSize, int binaryMessageMaximumSize) {
        this.buffer = new LimitedBuffer(input, textMessageMaximumSize, binaryMessageMaximumSize);
        this.channel = channel;
    }

    Event nextMessageEvent() throws IOException {
        int messageTypeOrFistCharacter = buffer.readByte();
        switch (messageTypeOrFistCharacter) {
            case '$':
                byte[] message = buffer.readBinaryLine();
                return binaryMessageEvent(message);
            case '\n':
                return textMessageEvent("");
            default:
                message = buffer.readTextLine();
                String textMessage = (messageTypeOrFistCharacter != '#') ? (char) messageTypeOrFistCharacter + new String(message) : new String(message);
                return textMessageEvent(textMessage);
        }
    }

    private TextMessageEvent textMessageEvent(String textMessage) {
        return new TextMessageEvent(channel, textMessage);
    }

    private BinaryMessageEvent binaryMessageEvent(byte[] message) throws IOException {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(message);
        } catch (IllegalArgumentException e) {
            throw new IOException("Problem during decoding binary message", e);
        }
        return new BinaryMessageEvent(channel, decoded);
    }

}
