package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class InputPipe {

    private static final Logger logger = Logger.getLogger(InputPipe.class.getName());

    private final MessageStream messageStream;
    private final Consumer<Exception> onError;
    private Thread thread;
    private volatile boolean stopped;

    public InputPipe(MessageStream messageStream, Consumer<Exception> onError) {
        this.onError = onError;
        this.messageStream = messageStream;
    }

    public void start() {
        thread = new Thread(() -> {
            try {
                while (!stopped) {
                    messageStream.readMessage();
                }
            } catch (InterruptedIOException e) {
                logger.info("Reading message stream interrupted");
            } catch (IOException e) {
                onError.accept(new StreamsMessageChannelException("Problem during reading input stream", e));
            }
        });
        thread.start();
    }

    public void stop() {
        stopped = true;
        if (thread != null) {
            thread.interrupt();
        }
    }

}
