package com.github.jacekolszak.messagic.streams

import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

class ConsumeOneMessage implements Consumer {

    private final CountDownLatch latch = new CountDownLatch(1)
    private Object message
    private boolean messageReceived

    @Override
    void accept(Object message) {
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

    Object message() {
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
