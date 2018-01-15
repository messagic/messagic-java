package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.ChannelAlreadyOpenException;
import com.github.jacekolszak.messagic.FatalError;
import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.NotOpenChannelException;

class StreamsMessageChannel implements MessageChannel {

    private final InputStream input;
    private final OutputStreamEncoder encoder;
    private boolean open;
    private Consumer<String> textConsumer;
    private Consumer<byte[]> binaryConsumer;
    private Consumer<FatalError> errorConsumer;
    private InputStreamDecoder decoder;
    private int binaryMessageMaximumSize = 8192;
    private int textMessageMaximumSize = 8192;
    private final Consumer<String> decodingErrorConsumer = this::sendError;

    StreamsMessageChannel(InputStream input, OutputStream output) {
        this.input = input;
        this.encoder = new OutputStreamEncoder(output);
    }

    @Override
    public void setBinaryMessageMaximumSize(int bytes) {
        this.binaryMessageMaximumSize = bytes;
    }

    @Override
    public void setTextMessageMaximumSize(int characters) {
        this.textMessageMaximumSize = characters;
    }

    @Override
    public void setBinaryMessageConsumer(Consumer<byte[]> consumer) {
        this.binaryConsumer = (msg) -> {
            try {
                consumer.accept(msg);
            } catch (RuntimeException e) {
                sendError(e.getMessage());
            }
        };
    }

    @Override
    public void setTextMessageConsumer(Consumer<String> consumer) {
        this.textConsumer = (msg) -> {
            try {
                consumer.accept(msg);
            } catch (RuntimeException e) {
                sendError(e.getMessage());
            }
        };
    }

    @Override
    public void setErrorConsumer(Consumer<FatalError> consumer) {
        this.errorConsumer = consumer;
    }

    @Override
    public void open() {
        if (open) {
            throw new ChannelAlreadyOpenException();
        }
        decoder = new InputStreamDecoder(input, textConsumer, binaryConsumer, errorConsumer, decodingErrorConsumer, binaryMessageMaximumSize, textMessageMaximumSize);
        decoder.start();
        open = true;
    }

    @Override
    public void close() {
        decoder.stop();
    }

    @Override
    public void send(byte[] message) {
        if (!open) {
            throw new NotOpenChannelException("Execute open() before sending any message");
        }
        if (message.length > binaryMessageMaximumSize) {
            errorConsumer.accept(new EndpointNotReachable("Payload of sent binary message exceeded maximum size"));
            close();
        } else {
            try {
                encoder.sendBinary(message);
            } catch (IOException e) {
                errorConsumer.accept(new EndpointNotReachable(e.getMessage()));
            }
        }
    }

    @Override
    public void send(String message) {
        if (!open) {
            throw new NotOpenChannelException("Execute open() before sending any message");
        }
        if (message.length() > textMessageMaximumSize) {
            errorConsumer.accept(new EndpointNotReachable("Payload of sent text message exceeded maximum size"));
            close();
        } else {
            try {
                encoder.sendText(message);
            } catch (IOException e) {
                errorConsumer.accept(new EndpointNotReachable(e.getMessage()));
            }
        }
    }

    private void sendError(String error) {
        try {
            String errorMessage = Optional.ofNullable(error).orElse("null");
            errorMessage = errorMessage.substring(0, Math.min(textMessageMaximumSize, errorMessage.length()));
            encoder.sendError(errorMessage);
        } catch (IOException e) {
            errorConsumer.accept(new EndpointNotReachable(e.getMessage()));
        }
    }

}
