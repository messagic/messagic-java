package com.github.messagic.streams;

import com.github.messagic.MessageChannel;
import com.github.messagic.Stopped;

final class StoppedEvent implements Stopped {

    private final StreamsMessageChannel channel;

    StoppedEvent(StreamsMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

}
