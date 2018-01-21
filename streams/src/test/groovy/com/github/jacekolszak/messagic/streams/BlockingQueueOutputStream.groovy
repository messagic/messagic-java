package com.github.jacekolszak.messagic.streams

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

final class BlockingQueueOutputStream extends OutputStream {

    private final BlockingQueue<Integer> bytes = new ArrayBlockingQueue<>(1024)
    private volatile boolean closed

    @Override
    void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        bytes.put(b)
    }

    // TODO This should be fixed - because now it does not care about UTF-8 encoding!
    String nextLine() {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        int character
        while ((character = bytes.take()) != '\n') {
            out.write(character)
        }
        return out.toString()
    }

    @Override
    void close() throws IOException {
        closed = true
    }

    int available() {
        return bytes.size()
    }

}
