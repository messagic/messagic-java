package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.InputStream;

final class LimitedBuffer {

    private final Buffer buffer;
    private final int binaryMessageMaximumSize;
    private final int textMessageMaximumSize;

    LimitedBuffer(InputStream input, int binaryMessageMaximumSize, int textMessageMaximumSize) {
        this.buffer = new Buffer(input);
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    byte[] readBinaryLine() throws IOException {
        byte[] message = buffer.readLine(binaryMessageMaximumSize);
        if (message == null) {
            throw new IOException("Payload of received binary message exceeded maximum size");
        }
        return message;
    }

    byte[] readTextLine() throws IOException {
        byte[] message = buffer.readLine(textMessageMaximumSize);
        if (message == null) {
            throw new IOException("Payload of received text message exceeded maximum size");
        }
        return message;
    }

    int readByte() throws IOException {
        return buffer.readByte();
    }
}
