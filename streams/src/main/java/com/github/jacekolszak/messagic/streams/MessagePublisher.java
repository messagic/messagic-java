package com.github.jacekolszak.messagic.streams;

interface MessagePublisher {

    void publish(String textMessage);

    void publish(byte[] binaryMessage);

}
