package com.github.jacekolszak.messagic.streams;

interface IncomingMessageListener {

    void textMessageFound(String textMessage);

    void binaryMessageFound(byte[] binaryMessage);

}
