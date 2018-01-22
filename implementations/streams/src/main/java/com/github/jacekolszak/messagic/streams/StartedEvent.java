package com.github.jacekolszak.messagic.streams;

import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.Started;

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
