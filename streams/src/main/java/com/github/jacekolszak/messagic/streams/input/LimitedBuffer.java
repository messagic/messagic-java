package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;
import java.io.InputStream;

final class LimitedBuffer {

    private final Buffer buffer;
    private final int binaryMessageMaximumSize;
    private final int textMessageMaximumSize;

    LimitedBuffer(InputStream input, int textMessageMaximumSize, int binaryMessageMaximumSize) {
        this.buffer = new Buffer(input);
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    byte[] readBinaryLine() throws IOException {
        return buffer.readLine(binaryMessageMaximumSize);
    }

    byte[] readTextLine() throws IOException {
        return buffer.readLine(textMessageMaximumSize);
    }

    int readByte() throws IOException {
        return buffer.readByte();
    }

}
