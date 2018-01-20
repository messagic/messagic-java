package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

final class InputPipe {

    private static final Logger logger = Logger.getLogger(InputPipe.class.getName());

    private final MessageStream messageStream;
    private final Consumer<Exception> onError;
    private Thread thread;
    private volatile boolean stopped;

    InputPipe(InputStream input, Limits limits, IncomingMessageListener incomingMessageListener, Consumer<Exception> onError) {
        this.onError = onError;
        this.messageStream = new MessageStream(input, limits, incomingMessageListener);
    }

    void start() {
        thread = new Thread(() -> {
            try {
                while (!stopped) {
                    messageStream.readMessage();
                }
            } catch (InterruptedIOException e) {
                logger.info("Reading message stream interrupted");
            } catch (IOException e) {
                onError.accept(new TextStreamsException("Problem during reading input stream", e));
            }
        });
        thread.start();
    }

    void stop() {
        stopped = true;
        if (thread != null) {
            thread.interrupt();
        }
    }

}
