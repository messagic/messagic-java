package com.github.jacekolszak.messagic.streams;

import java.io.IOException;

public class StreamsMessageChannelException extends IOException {

    public StreamsMessageChannelException(String message) {
        super(message);
    }

    public StreamsMessageChannelException(String message, Throwable cause) {
        super(message, cause);
    }

}
