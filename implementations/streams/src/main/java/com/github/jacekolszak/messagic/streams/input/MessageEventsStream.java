package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.TextMessage;

public final class MessageEventsStream {

    private final DecodingBuffer buffer;
    private final MessageChannel channel;

    public MessageEventsStream(DecodingBuffer buffer, MessageChannel channel) {
        this.buffer = buffer;
        this.channel = channel;
    }

    Event nextMessageEvent() throws IOException {
        char typeOrFistCharacter = buffer.nextChar();
        switch (typeOrFistCharacter) {
            case '$':
                return binaryMessageEvent(buffer.nextBinaryMessage());
            case '\n':
                return textMessageEvent("");
            case '#':
                return textMessageEvent(buffer.nextTextMessage());
            case '@':
                return textMessageEvent(buffer.nextMultiLineTextMessage());
            default:
                return textMessageEvent(typeOrFistCharacter + buffer.nextPartialTextMessage());
        }
    }

    private BinaryMessage binaryMessageEvent(byte[] message) {
        return new BinaryMessageEvent(channel, message);
    }

    private TextMessage textMessageEvent(String text) {
        return new TextMessageEvent(channel, text);
    }

}
