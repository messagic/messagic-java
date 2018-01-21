package com.github.jacekolszak.messagic.streams.input;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

final class Buffer {

    private final InputStream inputStream;

    Buffer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    byte[] readLine(int limit) throws IOException {
        int size = 0;
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (size < limit) {
            int b = readByte();
            if (b == '\n') {
                return buffer.toByteArray();
            } else {
                buffer.write(b);
                size += 1;
            }
        }
        throw new IOException("Payload of received message exceeded maximum size");
    }

    int readByte() throws IOException {
        int b = inputStream.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

}
