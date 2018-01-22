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
        if (b == EOF) {
            bytesQueue.put(EOF) // EOF should be always on the end of the queue
        } else if (b == IO_EXCEPTION) {
            throw new IOException("Fake IO exception")
        }
        return b
    }

    @Override
    int read(byte[] output, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        int availableBytes = Math.min(length, bytesQueue.size())
        if (availableBytes > 0) {
            return readBytes(output, offset, availableBytes)
        } else {
            return readByte(output, offset)
        }
    }

    private int readBytes(byte[] output, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            int b = readByte(output, i);
            if (b == EOF) {
                int bytesRead = i - offset
                return bytesRead
            }
        }
        return length
    }

    private int readByte(byte[] output, int offset) {
        int b = read()
        if (b != EOF) {
            output[offset] = (byte) b
            return 1
        } else {
            return EOF
        }
    }

    @Override
    void close() throws IOException {
        bytesQueue.put(EOF)
    }

    void readThrowsException() {
        bytesQueue.put(IO_EXCEPTION)
    }

}
