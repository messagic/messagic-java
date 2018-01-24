package com.github.messagic.streams;

import com.github.messagic.Error;
import com.github.messagic.MessageChannel;

final class ErrorEvent implements Error {

    private final MessageChannel channel;
    private final Exception exception;

    ErrorEvent(MessageChannel channel, Exception exception) {
        this.channel = channel;
        this.exception = exception;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

    @Override
    public Exception exception() {
        return exception;
    }

}
