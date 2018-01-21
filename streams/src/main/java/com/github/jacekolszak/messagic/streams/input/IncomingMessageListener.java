package com.github.jacekolszak.messagic.streams.input;

public interface IncomingMessageListener {

    void textMessageFound(String textMessage);

    void binaryMessageFound(byte[] binaryMessage);

}
