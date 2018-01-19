package com.github.jacekolszak.messagic.streams

import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

class ConsumeOneMessage<T> implements Consumer<T> {

    private final CountDownLatch latch = new CountDownLatch(1)
    private T message
    private boolean messageReceived

    @Override
    void accept(T message) {
        try {
            if (messageReceived) {
                throw new RuntimeException('Too many messages received. Expected only one')
            }
            this.messageReceived = true
            this.message = message
        } finally {
            latch.countDown()
        }
    }

    T message() {
        latch.await()
        if (!messageReceived) {
            throw new RuntimeException('Message not received')
        }
        return message
    }

    boolean messageReceived() {
        return messageReceived
    }

}
