package com.github.messagic.streams.examples;

import com.github.messagic.TextMessage;
import com.github.messagic.streams.StreamsMessageChannel;

final class StandardIOEcho {

    public static void main(String[] args) {
        StreamsMessageChannel channel = new StreamsMessageChannel(System.in, System.out);
        channel.addListener(TextMessage.class, event -> channel.send(event.text()));
        channel.start();
    }

}
