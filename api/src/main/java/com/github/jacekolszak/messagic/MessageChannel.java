package com.github.jacekolszak.messagic;

import java.util.function.Consumer;

/**
 * Abstraction of channel where messages are being sent in two directions between running parties.
 */
public interface MessageChannel {

    /**
     * @param consumer If consumer throws runtime exception then error message is sent to a peer
     */
    void setBinaryMessageConsumer(Consumer<byte[]> consumer);

    /**
     * @param consumer If consumer throws runtime exception then error message is sent to a peer
     */
    void setTextMessageConsumer(Consumer<String> consumer);

    /**
     * FatalError is returned when a peer thrown runtime exception during message consumption
     * or the peer was unreachable (down, had networks problems etc.).
     */
    void setErrorConsumer(Consumer<FatalError> consumer);

    void open();

    void send(String textMessage);

    void send(byte[] binaryMessage);

    void close();

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will close the
     * channel and report error
     *
     * @param bytes Number of bytes
     */
    void setBinaryMessageMaximumSize(int bytes);

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will close the
     * channel and report error. This property is also used when sending errors - first for trimming the error messages and second for validating size of error message on the receiver.
     *
     * @param characters Number of characters
     */
    void setTextMessageMaximumSize(int characters);

}
