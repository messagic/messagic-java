package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;

import com.github.jacekolszak.messagic.streams.input.DecodingBuffer;
import com.github.jacekolszak.messagic.streams.output.MessageFactory;

public final class Limits {

    private int textMessageMaximumSize = 4096;
    private int binaryMessageMaximumSize = 4096;

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param bytes Number of bytes. Default is 4096
     */
    public void setBinaryMessageMaximumSize(int bytes) {
        this.binaryMessageMaximumSize = bytes;
    }

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param characters Number of characters. Default is 4096
     */
    public void setTextMessageMaximumSize(int characters) {
        this.textMessageMaximumSize = characters;
    }

    MessageFactory messageFactory() {
        return new MessageFactory(textMessageMaximumSize, binaryMessageMaximumSize);
    }

    DecodingBuffer decodingBuffer(InputStream inputStream) {
        return new DecodingBuffer(inputStream, textMessageMaximumSize, binaryMessageMaximumSize);
    }

}
