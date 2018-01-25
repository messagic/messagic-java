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

import com.github.messagic.BinaryMessage
import com.github.messagic.Error
import com.github.messagic.Stopped
import com.github.messagic.TextMessage
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

@Timeout(5)
final class LimitsSpec extends Specification {

    private final BlockingQueueInputStream inputStream = new BlockingQueueInputStream()
    private final BlockingQueueOutputStream outputStream = new BlockingQueueOutputStream()
    private final ConsumeOneMessage<Error> errorListener = new ConsumeOneMessage()
    private final AwaitingConsumer stoppedListener = new AwaitingConsumer()
    private final ConsumeOneMessage<BinaryMessage> binaryMessageListener = new ConsumeOneMessage()
    private final ConsumeOneMessage<TextMessage> textMessageListener = new ConsumeOneMessage()

    @Subject
    private StreamsMessageChannel channel

    void cleanup() {
        channel.stop()
    }

    void 'should close the channel when incoming text message is too big'() {
        given:
            Limits limits = new Limits(textMessageMaximumSize: 1)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(Error, errorListener)
            channel.addListener(Stopped, stoppedListener)
            channel.start()
        when:
            inputStream.writeTextMessage('ab')
        then:
            errorListener.message().exception() instanceof StreamsMessageChannelException
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when incoming binary message is too big'() {
        given:
            Limits limits = new Limits(binaryMessageMaximumSize: 1)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(Error, errorListener)
            channel.addListener(Stopped, stoppedListener)
            channel.start()
        when:
            inputStream.writeBinaryMessage('AQI=')
        then:
            errorListener.message().exception() instanceof StreamsMessageChannelException
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when outgoing text message is too big'() {
        given:
            Limits limits = new Limits(textMessageMaximumSize: 1)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(Error, errorListener)
            channel.addListener(Stopped, stoppedListener)
            channel.start()
        when:
            channel.send('ab')
        then:
            errorListener.message().exception() instanceof StreamsMessageChannelException
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when outgoing binary message is too big'() {
        given:
            Limits limits = new Limits(binaryMessageMaximumSize: 1)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(Error, errorListener)
            channel.addListener(Stopped, stoppedListener)
            channel.start()
        when:
            channel.send([1, 2] as byte[])
        then:
            errorListener.message().exception() instanceof StreamsMessageChannelException
            stoppedListener.waitUntilExecuted()
    }

    void 'should be possible to read binary message which is short enough but his encoded version is longer than binaryMessageMaximumSize'() {
        given:
            Limits limits = new Limits(binaryMessageMaximumSize: 1)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(BinaryMessage, binaryMessageListener)
            channel.start()
        when:
            inputStream.writeBinaryMessage('AA==')
        then:
            binaryMessageListener.message().bytes() == [0] as byte[]
    }

    @Unroll
    void 'should be possible to read text message "#textMessage" which has maximum characters'() {
        given:
            Limits limits = new Limits(textMessageMaximumSize: 1)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(TextMessage, textMessageListener)
            channel.start()
        when:
            inputStream.writeTextMessage(textMessage)
        then:
            textMessageListener.message().text() == 'a'
        where:
            textMessage << ['a', '#a']
    }

    @Unroll
    void 'should be possible to read multi-line text message which has maximum characters'() {
        given:
            Limits limits = new Limits(textMessageMaximumSize: 2)
            channel = new StreamsMessageChannel(inputStream, outputStream, limits)
            channel.addListener(TextMessage, textMessageListener)
            channel.start()
        when:
            inputStream.writeMultiLineTextMessage('A\n')
        then:
            textMessageListener.message().text() == 'A\n'
    }

}
