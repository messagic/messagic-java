package com.github.jacekolszak.messagic;

public class ChannelAlreadyOpenException extends RuntimeException {

    public ChannelAlreadyOpenException() {
        super("Channel was open before. You can't open it twice");
    }
}
