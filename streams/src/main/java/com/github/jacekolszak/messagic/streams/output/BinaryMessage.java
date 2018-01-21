package com.github.jacekolszak.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

final class BinaryMessage {

    private final byte[] bytes;

    BinaryMessage(byte[] bytes) {
        this.bytes = bytes;
    }

    void encode(OutputStream output) throws IOException {
        output.write('$');
        output.write(Base64.getEncoder().encode(bytes));
        output.write('\n');
    }

}
