package com.github.jacekolszak.messagic.streams

final class StreamsPipedOutputStream extends PipedOutputStream {

    private final PipedInputStream inputStream

    StreamsPipedOutputStream(PipedInputStream inputStream) {
        super(inputStream)
        this.inputStream = inputStream
    }

    StreamsPipedOutputStream() {
        this(new PipedInputStream())
    }

    PipedInputStream inputStream() {
        return inputStream
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

}
