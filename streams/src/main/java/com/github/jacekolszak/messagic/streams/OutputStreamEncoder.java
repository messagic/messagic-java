package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

final class OutputStreamEncoder {

    private final OutputStream output;

    OutputStreamEncoder(OutputStream output) {
        this.output = output;
    }

    void sendText(String message) throws IOException {
        if (messageStartsWithSpecialCharacter(message)) {
            output.write('#');
        }
        output.write(message.getBytes());
        output.write('\n');
    }

    private boolean messageStartsWithSpecialCharacter(String message) {
        if (!message.isEmpty()) {
            char firstChar = message.charAt(0);
            return firstChar == '#' || firstChar == '$' || firstChar == '!';
        } else {
            return false;
        }
    }

    void sendError(String errorMessage) throws IOException {
        output.write('!');
        output.write(errorMessage.getBytes());
        output.write('\n');
    }

    void sendBinary(byte[] message) throws IOException {
        output.write('$');
        output.write(Base64.getEncoder().encode(message));
        output.write('\n');
    }
}
