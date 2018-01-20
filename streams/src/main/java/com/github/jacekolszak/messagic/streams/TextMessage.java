package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.OutputStream;

final class TextMessage {

    private final String text;

    TextMessage(String text) {
        this.text = text;
    }

    void encode(OutputStream output) throws IOException {
        if (messageStartsWithSpecialCharacter()) {
            output.write('#');
        }
        output.write(text.getBytes());
        output.write('\n');
    }

    private boolean messageStartsWithSpecialCharacter() {
        if (!text.isEmpty()) {
            char firstChar = text.charAt(0);
            return firstChar == '#' || firstChar == '$';
        } else {
            return false;
        }
    }

}
