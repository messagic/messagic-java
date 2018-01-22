package com.github.jacekolszak.messagic.streams.examples;

import com.github.jacekolszak.messagic.TextMessage;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannel;

final class StandardIOEcho {

    public static void main(String[] args) {
        StreamsMessageChannel channel = new StreamsMessageChannel(System.in, System.out);
        channel.eventBus().addListener(TextMessage.class, event -> channel.send(event.text()));
        channel.start();
    }

}
