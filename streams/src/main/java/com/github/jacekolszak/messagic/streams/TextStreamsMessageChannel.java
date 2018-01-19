package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.Lifecycle;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.MessageListeners;

public class TextStreamsMessageChannel implements MessageChannel {

    private final InputPipe input;
    private final OutputPipe output;
    private final TextStreamsMessageListeners messageConsumers = new TextStreamsMessageListeners();

    public TextStreamsMessageChannel(InputStream input, OutputStream output) {
        this.input = new InputPipe(input, messageConsumers);
        this.output = new OutputPipe(output);
    }

    @Override
    public Lifecycle lifecycle() {
        return null;
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
        input.start();
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
        try {
            input.stop();
        } finally {
            output.stop();
        }
    }

}