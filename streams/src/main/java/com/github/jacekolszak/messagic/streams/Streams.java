package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.FatalError;
import com.github.jacekolszak.messagic.MessageChannel;

public class Streams {

    private final InputStream input;
    private final OutputStream output;
    private final StreamsMessageChannel channel;

    public Streams(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        channel = new StreamsMessageChannel();
    }

    public MessageChannel channel() {
        return channel;
    }

    private class StreamsMessageChannel implements MessageChannel {

        private Consumer<String> textConsumer;
        private Consumer<byte[]> binaryConsumer;
        private Consumer<FatalError> errorConsumer;
        private InputStreamDecoder decoder;
        private int binaryMessageMaximumSize = 8192;
        private int textMessageMaximumSize = 8192;
        private Consumer<String> decodingErrorConsumer = error -> {
            sendError(error);
        };

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
            decoder = new InputStreamDecoder(input, textConsumer, binaryConsumer, errorConsumer, decodingErrorConsumer, binaryMessageMaximumSize, textMessageMaximumSize);
            decoder.start();
        }

        @Override
        public void close() {
            decoder.stop();
        }

        @Override
        public void send(byte[] message) {
            if (message.length > binaryMessageMaximumSize) {
                errorConsumer.accept(new EndpointNotReachable("Payload of sent binary message exceeded maximum size"));
                close();
            } else {
                try {
                    output.write('#');
                    output.write(Base64.getEncoder().encode(message));
                    output.write('\n');
                } catch (IOException e) {
                    errorConsumer.accept(new EndpointNotReachable(e.getMessage()));
                }
            }
        }

        @Override
        public void send(String message) {
            if (message.length() > textMessageMaximumSize) {
                errorConsumer.accept(new EndpointNotReachable("Payload of sent text message exceeded maximum size"));
                close();
            } else {
                try {
                    output.write(message.getBytes());
                    output.write('\n');
                } catch (IOException e) {
                    errorConsumer.accept(new EndpointNotReachable(e.getMessage()));
                }
            }
        }

        private void sendError(String error) {
            send('!' + error); // TODO hack
        }

    }

}
