package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

class MessageStream {

    private final LimitedBuffer buffer;
    private final MessagePublisher messagePublisher;

    MessageStream(InputStream input, Limits limits, MessagePublisher messagePublisher) {
        this.buffer = new LimitedBuffer(input, limits.binaryMessageMaximumSize, limits.textMessageMaximumSize);
        this.messagePublisher = messagePublisher;
    }

    void readMessage() throws IOException {
        int messageTypeOrFistCharacter = buffer.readByte();
        switch (messageTypeOrFistCharacter) {
            case '$':
                byte[] message = buffer.readBinaryLine();
                publishDecodedMessage(message);
                break;
            case '\n':
                messagePublisher.publish("");
                break;
            default:
                message = buffer.readTextLine();
                String textMessage = (messageTypeOrFistCharacter != '#') ? (char) messageTypeOrFistCharacter + new String(message) : new String(message);
                messagePublisher.publish(textMessage);
        }
    }

    private void publishDecodedMessage(byte[] message) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(message);
        } catch (IllegalArgumentException e) {
            return;
        }
        messagePublisher.publish(decoded);
    }

}
