package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.BinaryMessage
import com.github.jacekolszak.messagic.Error
import com.github.jacekolszak.messagic.Stopped
import com.github.jacekolszak.messagic.TextMessage
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

}
