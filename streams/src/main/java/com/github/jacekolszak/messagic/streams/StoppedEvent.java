package com.github.jacekolszak.messagic.streams;

import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.Stopped;

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
