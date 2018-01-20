package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.Started
import com.github.jacekolszak.messagic.Stopped
import spock.lang.Specification
import spock.lang.Subject

final class TextStreamsMessageChannelLifecycleEventsSpec extends Specification {

    private final TextStreamsPipedOutputStream inputPipe = new TextStreamsPipedOutputStream()
    private final PipedInputStream input = inputPipe.inputStream()
    private final TextStreamsPipedOutputStream output = new TextStreamsPipedOutputStream()

    @Subject
    private TextStreamsMessageChannel channel = new TextStreamsMessageChannel(input, output)

    void cleanup() {
        channel.stop()
    }

    void 'after start should notify StartedEvent listener'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.events().addListener(Started, listener)
        when:
            channel.start()
        then:
            listener.message() instanceof Started
    }

    void 'should notify StartedEvent listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.events().addListener(Started, first)
            channel.events().addListener(Started, last)
        when:
            channel.start()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    void 'all StartedEvent listeners should be executed even though some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.events().addListener(Started, first)
            channel.events().addListener(Started, last)
        when:
            channel.start()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }


    void 'after stop should notify StoppedEvent listener'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.events().addListener(Stopped, listener)
            channel.start()
        when:
            channel.stop()
        then:
            listener.message() instanceof Stopped
    }

    void 'should notify StoppedEvent listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.events().addListener(Stopped, first)
            channel.events().addListener(Stopped, last)
            channel.start()
        when:
            channel.stop()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    void 'all StoppedEvent listeners should be executed even though some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.events().addListener(Stopped, first)
            channel.events().addListener(Stopped, last)
            channel.start()
        when:
            channel.stop()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }


    void 'removed StartedEvent listeners does not receive notifications'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.events().addListener(Started, first)
            channel.events().addListener(Started, last)
        when:
            channel.events().removeListener(Started, first)
            channel.start()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

    void 'removed StoppedEvent listeners does not receive notifications'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.events().addListener(Stopped, first)
            channel.events().addListener(Stopped, last)
            channel.start()
        when:
            channel.events().removeListener(Stopped, first)
            channel.stop()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

}
