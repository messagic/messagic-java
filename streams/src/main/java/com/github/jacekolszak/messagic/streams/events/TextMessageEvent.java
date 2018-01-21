package com.github.jacekolszak.messagic.streams.events;

import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.TextMessage;

final class TextMessageEvent implements TextMessage {

    private final MessageChannel channel;
    private final String textMessage;

    TextMessageEvent(MessageChannel channel, String textMessage) {
        this.channel = channel;
        this.textMessage = textMessage;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

    @Override
    public String text() {
        return textMessage;
    }
}
