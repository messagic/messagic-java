package com.github.jacekolszak.messagic.streams

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

final class BlockingQueueInputStream extends InputStream {

    private static final int EOF = -1
    private static final int IO_EXCEPTION = -2

    private final BlockingQueue<Integer> bytesQueue = new ArrayBlockingQueue<>(1024)

    void write(byte[] bytes) {
        for (byte b : bytes) {
            this.bytesQueue.put((int) b);
        }
    }

    void writeTextMessage(String textMessage) {
        write("$textMessage\n".bytes)
    }

    void writeTextMessage() {
        writeTextMessage('message')
    }

    void writeBinaryMessage() {
        write('$AQID\n'.bytes)
    }

    void writeBinaryMessage(String base64) {
        write("\$$base64\n".bytes)
    }

    @Override
    int read() throws IOException {
        int b = bytesQueue.take()
        if (b == IO_EXCEPTION) {
            throw new IOException("Fake IO exception")
        }
        return b
    }

    @Override
    void close() throws IOException {
        bytesQueue.put(EOF)
    }

    void readThrowsException() {
        bytesQueue.put(IO_EXCEPTION)
    }

}
