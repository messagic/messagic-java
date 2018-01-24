package com.github.jacekolszak.messagic.streams.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

final class MessageStream {

    private final InputStreamReader reader;

    private String message;
    private boolean binaryMessage;

    MessageStream(InputStream inputStream) {
        try {
            this.reader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to create MessageStream because UTF-8 encoding is not supported", e);
        }
    }

    void readMessage(int textMessageMaximumSize, int binaryMessageMaximumSize) throws IOException {
        char typeOrFistCharacter = readChar();
        if (typeOrFistCharacter == '@') {
            message = readMultiLineMessage(textMessageMaximumSize);
            binaryMessage = false;
        } else if (typeOrFistCharacter == '$') {
            message = readMessage(binaryMessageMaximumSize);
            binaryMessage = true;
        } else if (typeOrFistCharacter == '#') {
            message = readMessage(textMessageMaximumSize);
            binaryMessage = false;
        } else if (typeOrFistCharacter == '\n') {
            message = "";
            binaryMessage = false;
        } else {
            message = typeOrFistCharacter + readMessage(textMessageMaximumSize - 1);
            binaryMessage = false;
        }
    }

    String message() {
        return message;
    }

    boolean binaryMessage() {
        return binaryMessage;
    }

    private String readMessage(int limit) throws IOException {
        int size = 0;
        final StringBuilder message = new StringBuilder();
        while (size < limit) {
            char c = readChar();
            if (c == '\n') {
                return message.toString();
            } else {
                message.append(c);
                size += 1;
            }
        }
        if (readChar() == '\n') {
            return message.toString();
        }
        throw new IOException("Received message exceeded maximum size of " + limit + " characters");
    }

    private String readMultiLineMessage(int limit) throws IOException {
        final StringBuilder message = new StringBuilder();
        while (message.length() <= limit) {
            String nextLine = readMessage(limit - message.length());
            if (nextLine.equals(".")) {
                return message.substring(0, message.length() - 1);
            } else if (nextLine.startsWith(".")) {
                nextLine = nextLine.substring(1);
            }
            message.append(nextLine).append('\n');
        }
        throw new IOException("Received message exceeded maximum size of " + limit + " characters");
    }

    private char readChar() throws IOException {
        int character = reader.read();
        if (character == -1) {
            throw new EOFException("InputStream is closed. Can't read from it.");
        }
        return (char) character;
    }

}
