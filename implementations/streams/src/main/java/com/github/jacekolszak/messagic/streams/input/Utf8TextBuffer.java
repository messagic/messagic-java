package com.github.jacekolszak.messagic.streams.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

final class Utf8TextBuffer {

    private final InputStreamReader reader;

    Utf8TextBuffer(InputStream inputStream) {
        try {
            this.reader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to create Utf8TextBuffer because UTF-8 encoding is not supported", e);
        }
    }

    String nextMessage(int limit) throws IOException {
        int size = 0;
        final StringBuilder message = new StringBuilder();
        while (size < limit) {
            char c = nextChar();
            if (c == '\n') {
                return message.toString();
            } else {
                message.append(c);
                size += 1;
            }
        }
        if (nextChar() == '\n') {
            return message.toString();
        }
        throw new IOException("Received message exceeded maximum size of " + limit + " characters");
    }

    String nextMultilineMessage(int limit) throws IOException {
        final StringBuilder message = new StringBuilder();
        while (message.length() <= limit) {
            String nextLine = nextMessage(limit - message.length());
            if (nextLine.equals(".")) {
                return message.substring(0, message.length() - 1);
            } else if (nextLine.startsWith(".")) {
                nextLine = nextLine.substring(1);
            }
            message.append(nextLine).append('\n');
        }
        throw new IOException("Received message exceeded maximum size of " + limit + " characters");
    }

    char nextChar() throws IOException {
        int character = reader.read();
        if (character == -1) {
            throw new EOFException("InputStream is closed. Can't read from it.");
        }
        return (char) character;
    }

}
