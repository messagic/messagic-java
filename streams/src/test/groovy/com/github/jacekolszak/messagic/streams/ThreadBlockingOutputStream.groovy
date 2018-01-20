package com.github.jacekolszak.messagic.streams

final class ThreadBlockingOutputStream extends OutputStream {

    @Override
    synchronized void write(int b) throws IOException {
        wait();
    }

}
