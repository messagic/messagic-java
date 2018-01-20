package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.Error
import com.github.jacekolszak.messagic.StoppedEvent
import spock.lang.Specification
import spock.lang.Subject

final class TextStreamsMessageChannelLimitsSpec extends Specification {

    private final TextStreamsPipedOutputStream inputPipe = new TextStreamsPipedOutputStream()
    private final PipedInputStream input = inputPipe.inputStream()
    private final TextStreamsPipedOutputStream output = new TextStreamsPipedOutputStream()
    private final ConsumeOneMessage<Error> errorListener = new ConsumeOneMessage()
    private final AwaitingConsumer stoppedListener = new AwaitingConsumer()

    @Subject
    private TextStreamsMessageChannel channel

    void cleanup() {
        channel.stop()
    }

    void 'should close the channel when incoming text message is too big'() {
        given:
            Limits limits = new Limits(textMessageMaximumSize: 1)
            channel = new TextStreamsMessageChannel(input, output, limits)
            channel.events().addListener(Error, errorListener)
            channel.events().addListener(StoppedEvent, stoppedListener)
            channel.start()
        when:
            inputPipe.writeTextMessage('ab')
        then:
            errorListener.message().exception() instanceof TextStreamsException
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when incoming binary message is too big'() {
        given:
            Limits limits = new Limits(binaryMessageMaximumSize: 1)
            channel = new TextStreamsMessageChannel(input, output, limits)
            channel.events().addListener(Error, errorListener)
            channel.events().addListener(StoppedEvent, stoppedListener)
            channel.start()
        when:
            inputPipe.writeBinaryMessage('AQI=')
        then:
            errorListener.message().exception() instanceof TextStreamsException
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when outgoing text message is too big'() {
        given:
            Limits limits = new Limits(textMessageMaximumSize: 1)
            channel = new TextStreamsMessageChannel(input, output, limits)
            channel.events().addListener(Error, errorListener)
            channel.events().addListener(StoppedEvent, stoppedListener)
            channel.start()
        when:
            channel.send('ab')
        then:
            errorListener.message().exception() instanceof TextStreamsException
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when outgoing binary message is too big'() {
        given:
            Limits limits = new Limits(binaryMessageMaximumSize: 1)
            channel = new TextStreamsMessageChannel(input, output, limits)
            channel.events().addListener(Error, errorListener)
            channel.events().addListener(StoppedEvent, stoppedListener)
            channel.start()
        when:
            channel.send([1, 2] as byte[])
        then:
            errorListener.message().exception() instanceof TextStreamsException
            stoppedListener.waitUntilExecuted()
    }

}
