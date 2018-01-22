package com.github.jacekolszak.messagic.streams.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

final class Utf8TextBuffer {

    private Utf8EncodedStream stream;

    Utf8TextBuffer(InputStream inputStream) {
        this.stream = new Utf8EncodedStream(inputStream);
    }

    String nextMessage(int limit) throws IOException {
        int size = 0;
        final StringBuilder builder = new StringBuilder();
        while (size < limit) {
            char c = nextChar();
            if (c == '\n') {
                return builder.toString();
            } else {
                builder.append(c);
                size += 1;
            }
        }
        if (nextChar() == '\n') {
            return builder.toString();
        }
        throw new IOException("Received message exceeded maximum size of " + limit + " characters");
    }

    char nextChar() throws IOException {
        return stream.nextCharacter();
    }

    // TODO This class is ugly. How is that possible that there is no way to read character as soon as it is available in stream? (InputStreamReader blocks infinitely because of excessive buffering)
    private final class Utf8EncodedStream {

        private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        private final InputStream inputStream;

        private Utf8EncodedStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        char nextCharacter() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(6);
            int b = nextByte();
            buffer.put((byte) b);
            if ((b >> 5) == 0b110) { //
                nextBytes(1, buffer);
            } else if ((b >> 4) == 0b1110) {
                nextBytes(2, buffer);
            } else if ((b >> 3) == 0b11110) {
                nextBytes(3, buffer);
            } else if ((b >> 2) == 0b111110) {
                nextBytes(4, buffer);
            } else if ((b >> 1) == 0b1111110) {
                nextBytes(5, buffer);
            }
            buffer.flip();
            return decoder.decode(buffer).get();
        }

        private int nextByte() throws IOException {
            int b = inputStream.read();
            if (b == -1) {
                throw new EOFException("InputStream is closed. Can't read from it.");
            }
            return b;
        }

        private void nextBytes(int numberOfBytes, ByteBuffer buffer) throws IOException {
            for (int i = 0; i < numberOfBytes; i++) {
                int b = nextByte();
                buffer.put((byte) b);
            }
        }

    }

}
