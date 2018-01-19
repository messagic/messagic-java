package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.Lifecycle;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.MessageListeners;

public class TextStreamsMessageChannel implements MessageChannel {

    private final InputPipe input;
    private final OutputPipe output;
    private final TextStreamsLifecycle lifecycle;
    private final TextStreamsMessageListeners messageConsumers = new TextStreamsMessageListeners();

    public TextStreamsMessageChannel(InputStream input, OutputStream output) {
        this.input = new InputPipe(input, messageConsumers);
        this.output = new OutputPipe(output);
        this.lifecycle = new TextStreamsLifecycle(this,
                this.input::start,
                () -> {
                    try {
                        this.input.stop();
                    } finally {
                        this.output.stop();
                    }
                });
    }

    @Override
    public Lifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public MessageListeners messageListeners() {
        return messageConsumers;
    }

    @Override
    public void start() {
        start(new Limits());
    }

    public void start(Limits limits) {
        lifecycle.start();
    }

    @Override
    public void send(String textMessage) {
        output.send(textMessage);
    }

    @Override
    public void send(byte[] binaryMessage) {
        output.send(binaryMessage);
    }

    @Override
    public void stop() {
        lifecycle.stop();
    }

}