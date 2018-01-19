package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

class MessageStream {

    private final Buffer buffer;
    private final int binaryMessageMaximumSize;
    private final int textMessageMaximumSize;
    private final MessagePublisher messagePublisher;

    MessageStream(InputStream input, int binaryMessageMaximumSize, int textMessageMaximumSize, MessagePublisher messagePublisher) {
        this.buffer = new Buffer(input);
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.textMessageMaximumSize = textMessageMaximumSize;
        this.messagePublisher = messagePublisher;
    }

    void readMessage() throws IOException {
        int messageTypeOrFistCharacter = buffer.readByte();
        switch (messageTypeOrFistCharacter) {
            case '$':
                byte[] message = buffer.readLine(binaryMessageMaximumSize);
                if (message == null) {
                    throw new IOException("Payload of received binary message exceeded maximum size");
                }
                publishDecodedMessage(message);
                break;
            case '\n':
                messagePublisher.publish("");
                break;
            default:
                message = buffer.readLine(textMessageMaximumSize);
                if (message == null) {
                    throw new IOException("Payload of received text message exceeded maximum size");
                }
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
