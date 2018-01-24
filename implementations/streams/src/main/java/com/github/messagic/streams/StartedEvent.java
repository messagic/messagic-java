package com.github.messagic.streams;

import com.github.messagic.MessageChannel;
import com.github.messagic.Started;

final class StartedEvent implements Started {

    private final StreamsMessageChannel channel;

    StartedEvent(StreamsMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

}
