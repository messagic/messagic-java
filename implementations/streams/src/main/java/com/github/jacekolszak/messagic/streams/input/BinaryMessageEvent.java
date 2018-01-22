package com.github.jacekolszak.messagic.streams.input;

import com.github.jacekolszak.messagic.BinaryMessage;
import com.github.jacekolszak.messagic.MessageChannel;

final class BinaryMessageEvent implements BinaryMessage {

    private final MessageChannel channel;
    private final byte[] binaryMessage;

    BinaryMessageEvent(MessageChannel channel, byte[] binaryMessage) {
        this.channel = channel;
        this.binaryMessage = binaryMessage;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

    @Override
    public byte[] bytes() {
        return binaryMessage;

    }
}
