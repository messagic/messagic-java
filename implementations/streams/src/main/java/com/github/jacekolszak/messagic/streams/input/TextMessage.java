package com.github.jacekolszak.messagic.streams.input;

import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

final class TextMessage implements Message {

    private final String encodedMessage;
    private final MessageChannel channel;
    private final int textMessageMaximumSize;

    TextMessage(MessageChannel channel, String encodedMessage, int textMessageMaximumSize) {
        this.encodedMessage = encodedMessage;
        this.channel = channel;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    @Override
    public DecodedMessage decodedMessage() throws StreamsMessageChannelException {
        if (encodedMessage.length() > textMessageMaximumSize) {
            String error = String.format("Incoming text message \"%s...\" is bigger than allowed %s characters",
                    encodedMessage.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return () -> new TextMessageEvent(channel, encodedMessage);
    }

}
