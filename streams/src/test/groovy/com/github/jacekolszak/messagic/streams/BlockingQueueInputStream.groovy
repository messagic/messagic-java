package com.github.jacekolszak.messagic.streams

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

final class BlockingQueueInputStream extends InputStream {

    private static final String ENCODING = "UTF-8"
    private static final int EOF = -1
    private static final int IO_EXCEPTION = -2
    private final BlockingQueue<Integer> bytesQueue = new ArrayBlockingQueue<>(1024)

    void write(byte[] bytes) {
        for (byte b : bytes) {
            this.bytesQueue.put(Byte.toUnsignedInt(b));
        }
    }

    void writeTextMessage(String textMessage) {
        write((textMessage + '\n').getBytes(ENCODING))
    }

    void writeTextMessage() {
        writeTextMessage('message')
    }

    void writeBinaryMessage() {
        write('$AQID\n'.getBytes(ENCODING))
    }

    void writeBinaryMessage(String base64) {
        write("\$$base64\n".getBytes(ENCODING))
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
