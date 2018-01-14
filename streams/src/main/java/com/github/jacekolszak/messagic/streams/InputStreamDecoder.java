package com.github.jacekolszak.messagic.streams;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Base64;
import java.util.function.Consumer;

import com.github.jacekolszak.messagic.FatalError;

class InputStreamDecoder {

    private final Consumer<String> textConsumer;
    private final Consumer<byte[]> binaryConsumer;
    private final Consumer<FatalError> errorConsumer;
    private final Consumer<String> decodingErrorConsumer;
    private final int binaryMessageMaximumSize;
    private final int textMessageMaximumSize;
    private final InputStream input;
    private Thread thread;
    private volatile boolean stopped;

    InputStreamDecoder(InputStream input, Consumer<String> textConsumer, Consumer<byte[]> binaryConsumer, Consumer<FatalError> errorConsumer, Consumer<String> decodingErrorConsumer, int binaryMessageMaximumSize, int textMessageMaximumSize) {
        this.input = input;
        this.textConsumer = textConsumer;
        this.binaryConsumer = binaryConsumer;
        this.errorConsumer = errorConsumer;
        this.decodingErrorConsumer = decodingErrorConsumer;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    void start() {
        thread = new Thread(() -> {
            MessageStream stream = new MessageStream(input);
            try {
                while (!stopped) {
                    stream.readMessage(textConsumer, binaryConsumer, decodingErrorConsumer);
                }
            } catch (InterruptedIOException e) {
                return;
            } catch (IOException e) {
                errorConsumer.accept(new EndpointNotReachable(e.getMessage()));
            }
        });
        thread.start();
    }

    void stop() {
        stopped = true;
        thread.interrupt();
    }

    class MessageStream {

        private final Buffer buffer;

        MessageStream(InputStream input) {
            this.buffer = new Buffer(input);
        }

        void readMessage(Consumer<String> textConsumer, Consumer<byte[]> binaryConsumer, Consumer<String> decodingErrorConsumer) throws IOException {
            int messageTypeOrFistCharacter = buffer.readByte();
            if (messageTypeOrFistCharacter == '#') {
                byte[] message = buffer.readUntil((byte) '\n', binaryMessageMaximumSize);
                if (message == null) {
                    throw new IOException("Payload of received binary message exceeded maximum size");
                }
                publishDecodedMessage(binaryConsumer, message, decodingErrorConsumer);
            } else if (messageTypeOrFistCharacter == '!') {
                byte[] message = buffer.readUntil((byte) '\n', textMessageMaximumSize);
                if (message == null) {
                    throw new IOException("Payload of received error message exceeded maximum size");
                }
                errorConsumer.accept(new ConsumerError(new String(message)));
            } else {
                byte[] message = buffer.readUntil((byte) '\n', textMessageMaximumSize);
                if (message == null) {
                    throw new IOException("Payload of received text message exceeded maximum size");
                }
                textConsumer.accept((char) messageTypeOrFistCharacter + new String(message));
            }
        }

        private void publishDecodedMessage(Consumer<byte[]> binaryConsumer, byte[] message, Consumer<String> decodingErrorConsumer) {
            byte[] decoded = null;
            try {
                decoded = Base64.getDecoder().decode(message);
            } catch (IllegalArgumentException e) {
                decodingErrorConsumer.accept("Bad encoding of incoming binary message: " + e.getMessage());
                return;
            }
            binaryConsumer.accept(decoded);
        }
    }

    class Buffer {

        private final BufferedInputStream input;

        Buffer(InputStream inputStream) {
            input = new BufferedInputStream(inputStream);
        }

        public byte[] readUntil(byte separator, int limit) throws IOException {
            int size = 0;
            input.mark(limit);
            while (size < limit) {
                int b = input.read();
                if (b == -1) {
                    throw new EOFException();
                } else if (b == separator) {
                    input.reset();
                    byte[] message = new byte[size];
                    if (input.read(message) == -1) {
                        throw new EOFException();
                    }
                    if (input.read() == -1) {
                        throw new EOFException();
                    }
                    return message;
                } else {
                    size += 1;
                }
            }
            return null;
        }

        public int readByte() throws IOException {
            int b = input.read();
            if (b == -1) {
                throw new EOFException();
            }
            return b;
        }
    }

    class ConsumerError implements FatalError {

        private String message;

        ConsumerError(String message) {
            this.message = message;
        }

        @Override
        public boolean isPeerError() {
            return true;
        }

        @Override
        public String message() {
            return message;
        }
    }

}
