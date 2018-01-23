package com.github.jacekolszak.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;

final class MultilineTextMessage implements TextMessage {

    private final String text;

    MultilineTextMessage(String text) {
        this.text = text;
    }

    @Override
    public void encode(OutputStream output) throws IOException {
        output.write('@');
        String escapedText = text.replace("\n.", "\n..");
        output.write(escapedText.getBytes("UTF-8"));
        output.write('\n');
        output.write('.');
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
