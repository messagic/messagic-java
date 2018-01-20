package com.github.jacekolszak.messagic.streams.events;

public interface IncomingMessageListener {

    void textMessageFound(String textMessage);

    void binaryMessageFound(byte[] binaryMessage);

}
