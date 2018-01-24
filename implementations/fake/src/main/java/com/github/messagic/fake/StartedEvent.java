package com.github.messagic.fake;

import com.github.messagic.MessageChannel;
import com.github.messagic.Started;

final class StartedEvent implements Started {

    private final FakeMessageChannel channel;

    StartedEvent(FakeMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

}
