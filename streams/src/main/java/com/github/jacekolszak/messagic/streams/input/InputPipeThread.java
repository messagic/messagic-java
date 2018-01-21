package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.github.jacekolszak.messagic.Event;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class InputPipeThread {

    private static final Logger logger = Logger.getLogger(InputPipeThread.class.getName());

    private final MessageEventsStream messageEventsStream;
    private final Consumer<Event> onMessage;
    private final Consumer<Exception> onError;
    private Thread thread;
    private volatile boolean stopped;

    public InputPipeThread(MessageEventsStream messageEventsStream, Consumer<Event> onMessage, Consumer<Exception> onError) {
        this.messageEventsStream = messageEventsStream;
        this.onMessage = onMessage;
        this.onError = onError;
    }

    public void start() {
        thread = new Thread(() -> {
            try {
                while (!stopped) {
                    Event messageEvent = messageEventsStream.nextMessageEvent();
                    onMessage.accept(messageEvent);
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
