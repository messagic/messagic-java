/*
 * Copyright 2018 The Messagic Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.messagic.streams.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.github.messagic.MessageChannel;
import com.github.messagic.streams.StreamsMessageChannelException;

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
                String message = readMultiLineMessage(textMessageMaximumSize + 2);
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
        if (message == null) {
            throw new IllegalStateException("Run readNextMessage() first");
        }
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
