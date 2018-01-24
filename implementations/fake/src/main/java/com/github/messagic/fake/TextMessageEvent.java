package com.github.messagic.fake;

import com.github.messagic.MessageChannel;
import com.github.messagic.TextMessage;

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
