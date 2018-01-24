package com.github.jacekolszak.messagic.streams.input;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

final class TextMessage implements Message {

    private final String message;
    private final MessageChannel channel;
    private final int textMessageMaximumSize;

    TextMessage(MessageChannel channel, String message, int textMessageMaximumSize) {
        this.message = message;
        this.channel = channel;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    @Override
    public void decode() throws StreamsMessageChannelException {
        if (message.length() > textMessageMaximumSize) {
            String error = String.format("Incoming text message \"%s...\" is bigger than allowed %s characters",
                    message.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
    }

    @Override
    public Event event() {
        return new TextMessageEvent(channel, message);
    }

}
