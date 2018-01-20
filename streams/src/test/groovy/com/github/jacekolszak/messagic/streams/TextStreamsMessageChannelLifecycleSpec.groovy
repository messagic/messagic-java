package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.StartedEvent
import com.github.jacekolszak.messagic.StoppedEvent
import spock.lang.Specification
import spock.lang.Subject

final class TextStreamsMessageChannelLifecycleSpec extends Specification {

    private final TextStreamsPipedOutputStream inputPipe = new TextStreamsPipedOutputStream()
    private final PipedInputStream input = inputPipe.inputStream()
    private final TextStreamsPipedOutputStream output = new TextStreamsPipedOutputStream()

    @Subject
    private TextStreamsMessageChannel channel = new TextStreamsMessageChannel(input, output)

    void cleanup() {
        channel.stop()
    }

    void 'after start should notify lifecycle listener with StartedEvent'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.lifecycle().addEventListener(StartedEvent, listener)
        when:
            channel.start()
        then:
            listener.message() instanceof StartedEvent
    }

    void 'should notify StartedEvent lifecycle listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.lifecycle().addEventListener(StartedEvent, first)
            channel.lifecycle().addEventListener(StartedEvent, last)
        when:
            channel.start()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    void 'all StartedEvent lifecycle listeners should be executed even though some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.lifecycle().addEventListener(StartedEvent, first)
            channel.lifecycle().addEventListener(StartedEvent, last)
        when:
            channel.start()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }


    void 'after stop should notify lifecycle listener with StoppedEvent'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.lifecycle().addEventListener(StoppedEvent, listener)
            channel.start()
        when:
            channel.stop()
        then:
            listener.message() instanceof StoppedEvent
    }

    void 'should notify StoppedEvent lifecycle listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.lifecycle().addEventListener(StoppedEvent, first)
            channel.lifecycle().addEventListener(StoppedEvent, last)
            channel.start()
        when:
            channel.stop()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    void 'all StoppedEvent lifecycle listeners should be executed even though some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.lifecycle().addEventListener(StoppedEvent, first)
            channel.lifecycle().addEventListener(StoppedEvent, last)
            channel.start()
        when:
            channel.stop()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }


    void 'removed StartedEvent lifecycle listeners does not receive notifications'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.lifecycle().addEventListener(StartedEvent, first)
            channel.lifecycle().addEventListener(StartedEvent, last)
        when:
            channel.lifecycle().removeEventListener(StartedEvent, first)
            channel.start()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

    void 'removed StoppedEvent lifecycle listeners does not receive notifications'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.lifecycle().addEventListener(StoppedEvent, first)
            channel.lifecycle().addEventListener(StoppedEvent, last)
            channel.start()
        when:
            channel.lifecycle().removeEventListener(StoppedEvent, first)
            channel.stop()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

}
