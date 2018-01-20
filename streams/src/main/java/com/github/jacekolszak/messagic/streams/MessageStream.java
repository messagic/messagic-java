package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.github.jacekolszak.messagic.streams.events.IncomingMessageListener;

final class MessageStream {

    private final LimitedBuffer buffer;
    private final IncomingMessageListener incomingMessageListener;

    MessageStream(InputStream input, Limits limits, IncomingMessageListener incomingMessageListener) {
        this.buffer = new LimitedBuffer(input, limits.binaryMessageMaximumSize, limits.textMessageMaximumSize);
        this.incomingMessageListener = incomingMessageListener;
    }

    void readMessage() throws IOException {
        int messageTypeOrFistCharacter = buffer.readByte();
        switch (messageTypeOrFistCharacter) {
            case '$':
                byte[] message = buffer.readBinaryLine();
                publishDecodedMessage(message);
                break;
            case '\n':
                incomingMessageListener.textMessageFound("");
                break;
            default:
                message = buffer.readTextLine();
                String textMessage = (messageTypeOrFistCharacter != '#') ? (char) messageTypeOrFistCharacter + new String(message) : new String(message);
                incomingMessageListener.textMessageFound(textMessage);
        }
    }

    private void publishDecodedMessage(byte[] message) throws IOException {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(message);
        } catch (IllegalArgumentException e) {
            throw new IOException("Problem during decoding binary message", e);
        }
        incomingMessageListener.binaryMessageFound(decoded);
    }

}
