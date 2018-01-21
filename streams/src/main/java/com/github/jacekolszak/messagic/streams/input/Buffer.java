package com.github.jacekolszak.messagic.streams.input;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

final class Buffer {

    private final BufferedInputStream input;

    Buffer(InputStream inputStream) {
        input = new BufferedInputStream(inputStream);
    }

    byte[] readLine(int limit) throws IOException {
        int size = 0;
        input.mark(limit);
        while (size < limit) {
            int b = readByte();
            if (b == '\n') {
                input.reset();
                byte[] message = new byte[size];
                if (input.read(message) == -1) {
                    throw new EOFException();
                }
                readByte();
                return message;
            } else {
                size += 1;
            }
        }
        return null;
    }

    int readByte() throws IOException {
        int b = input.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }
}
