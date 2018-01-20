package com.github.jacekolszak.messagic.streams

import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

final class ConsumeManyMessages implements Consumer {

    private final CountDownLatch latch
    private final List messages = []

    ConsumeManyMessages(int howMany) {
        this.latch = new CountDownLatch(howMany)
    }

    @Override
    void accept(Object t) {
        messages << t
        latch.countDown()
    }

    List<Object> messages() {
        latch.await()
        return messages
    }

}
