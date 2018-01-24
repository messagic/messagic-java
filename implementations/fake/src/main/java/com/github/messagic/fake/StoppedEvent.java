package com.github.messagic.fake;

import com.github.messagic.MessageChannel;
import com.github.messagic.Stopped;

final class StoppedEvent implements Stopped {

    private final FakeMessageChannel channel;

    StoppedEvent(FakeMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

}
