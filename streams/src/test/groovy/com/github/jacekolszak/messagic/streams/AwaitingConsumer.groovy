package com.github.jacekolszak.messagic.streams

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class AwaitingConsumer implements Consumer {

    private final CountDownLatch latch = new CountDownLatch(1)
    private final Consumer<AwaitingConsumer> consumer

    AwaitingConsumer(Consumer<AwaitingConsumer> consumer) {
        this.consumer = consumer
    }

    @Override
    void accept(Object t) {
        try {
            consumer.accept(this)
        } finally {
            latch.countDown()
        }
    }

    boolean waitUntilExecuted() {
        return latch.await(2, TimeUnit.SECONDS)
    }

}
