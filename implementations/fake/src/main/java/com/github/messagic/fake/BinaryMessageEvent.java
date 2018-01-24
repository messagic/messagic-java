package com.github.messagic.fake;

import com.github.messagic.BinaryMessage;
import com.github.messagic.MessageChannel;

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
