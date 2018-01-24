package com.github.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;

final class OneLineTextMessage implements TextMessage {

    private final String text;

    OneLineTextMessage(String text) {
        this.text = text;
    }

    @Override
    public void encode(OutputStream output) throws IOException {
        if (messageStartsWithSpecialCharacter()) {
            output.write('#');
        }
        output.write(text.getBytes("UTF-8"));
        output.write('\n');
    }

    private boolean messageStartsWithSpecialCharacter() {
        if (!text.isEmpty()) {
            char firstChar = text.charAt(0);
            return firstChar == '#' || firstChar == '$' || firstChar == '@';
        } else {
            return false;
        }
    }

}
