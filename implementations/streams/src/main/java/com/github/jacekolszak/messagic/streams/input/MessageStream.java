package com.github.jacekolszak.messagic.streams.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.github.jacekolszak.messagic.MessageChannel;
import com.github.jacekolszak.messagic.streams.StreamsMessageChannelException;

public final class MessageStream {

    private final InputStreamReader reader;
    private final MessageChannel channel;
    private final int textMessageMaximumSize;
    private final int binaryMessageMaximumSize;

    private Message message;

    public MessageStream(InputStream inputStream, MessageChannel channel, int textMessageMaximumSize,
                         int binaryMessageMaximumSize) {
        this.channel = channel;
        this.textMessageMaximumSize = textMessageMaximumSize;
        this.binaryMessageMaximumSize = binaryMessageMaximumSize;
        try {
            this.reader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to create MessageStream because UTF-8 encoding is not supported", e);
        }
    }

    void readNextMessage() throws IOException {
        char typeOrFistCharacter = readChar();
        switch (typeOrFistCharacter) {
            case '@':
                String message = readMultiLineMessage(textMessageMaximumSize);
                this.message = new TextMessage(channel, message, textMessageMaximumSize);
                break;
            case '$':
                int encodedBinaryMessageMaximumSize = (int) ((binaryMessageMaximumSize * 1.3) + 3);
                message = readMessage(encodedBinaryMessageMaximumSize);
                this.message = new BinaryMessage(channel, message, binaryMessageMaximumSize);
                break;
            case '#':
                message = readMessage(textMessageMaximumSize);
                this.message = new TextMessage(channel, message, textMessageMaximumSize);
                break;
            case '\n':
                this.message = new TextMessage(channel, "", textMessageMaximumSize);
                break;
            default:
                message = typeOrFistCharacter + readMessage(textMessageMaximumSize - 1);
                this.message = new TextMessage(channel, message, textMessageMaximumSize);
        }
    }

    Message message() {
        return message;
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
        throw new StreamsMessageChannelException("Received message exceeded maximum size of " + limit + " characters");
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
        throw new StreamsMessageChannelException("Received message exceeded maximum size of " + limit + " characters");
    }

    private char readChar() throws IOException {
        int character = reader.read();
        if (character == -1) {
            throw new EOFException("InputStream is closed. Can't read from it.");
        }
        return (char) character;
    }

}
