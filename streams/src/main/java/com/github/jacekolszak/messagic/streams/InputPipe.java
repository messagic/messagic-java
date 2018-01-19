package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;

class InputPipe {

    private final MessageStream messageStream;
    private Thread thread;

    InputPipe(InputStream input, MessagePublisher messagePublisher) {
        this.messageStream = new MessageStream(input, 1024, 1024, messagePublisher);
    }

    void start() {
        thread = new Thread(() -> {
            try {
                messageStream.readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void stop() {

    }

}
