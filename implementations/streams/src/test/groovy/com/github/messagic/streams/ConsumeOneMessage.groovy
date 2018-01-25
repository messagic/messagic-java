/*
 * Copyright 2018 The Messagic Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.messagic.streams

import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

final class ConsumeOneMessage<T> implements Consumer<T> {

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
