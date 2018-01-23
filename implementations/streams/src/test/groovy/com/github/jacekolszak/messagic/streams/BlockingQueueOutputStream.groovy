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

    String nextLine() {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        int character
        while ((character = bytes.take()) != '\n') {
            out.write(character)
        }
        return out.toString("UTF-8") + '\n'
    }

    List<String> nextLines(int count) {
        return (0..count).collect { nextLine() }
    }

    @Override
    void close() throws IOException {
        closed = true
    }

    int available() {
        return bytes.size()
    }

}
