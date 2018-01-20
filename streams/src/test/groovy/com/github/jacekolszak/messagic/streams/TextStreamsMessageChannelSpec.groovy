package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.StartedEvent
import com.github.jacekolszak.messagic.StoppedEvent
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

@Timeout(5)
class TextStreamsMessageChannelSpec extends Specification {

    private final PipedInputStream input = new PipedInputStream()
    private final TextStreamsPipedOutputStream inputPipe = new TextStreamsPipedOutputStream(input)
    private final PipedInputStream outputPipe = new PipedInputStream()
    private final TextStreamsPipedOutputStream output = new TextStreamsPipedOutputStream(outputPipe)
    private final InputStreamReader outputReader = new InputStreamReader(outputPipe)

    @Subject
    private final TextStreamsMessageChannel channel = new TextStreamsMessageChannel(input, output)

    void cleanup() {
        channel.stop()
    }

    void 'should send text message to output stream'() {
        given:
            channel.start()
        when:
            channel.send('textMessage')
        then:
            outputReader.readLine() == 'textMessage'
    }

    void 'should send binary message to output stream'() {
        given:
            channel.start()
        when:
            channel.send([1, 2, 3] as byte[])
        then:
            outputReader.readLine() == '$AQID'
    }

    void 'sending text message should be asynchronous'() {
        given:
            TextStreamsMessageChannel channel = new TextStreamsMessageChannel(input, new ThreadBlockingOutputStream())
            channel.start()
        when:
            channel.send('textMessage')
        then:
            true
    }

    void 'sending binary message should be asynchronous'() {
        given:
            TextStreamsMessageChannel channel = new TextStreamsMessageChannel(input, new ThreadBlockingOutputStream())
            channel.start()
        when:
            channel.send([1, 2, 3] as byte[])
        then:
            true
    }

    void 'should send empty text message to output stream'() {
        given:
            channel.start()
        when:
            channel.send('')
        then:
            outputReader.readLine() == ''
    }

    void 'should send empty binary message to output stream'() {
        given:
            channel.start()
        when:
            channel.send(new byte[0])
        then:
            outputReader.readLine() == '$'
    }

    @Unroll
    void 'should send text message "#message" encoded as "#line\\n"'() {
        given:
            channel.start()
        when:
            channel.send(message)
        then:
            outputReader.readLine() == line
        where:
            message    || line
            '#message' || '##message'
            '$message' || '#$message'
    }

    @Unroll
    void 'should read encoded text message "#inputString" from input stream and notify listener with "#expectedMessage"'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.incomingStream().addTextMessageListener(listener)
            channel.start()
        when:
            inputPipe.write(inputString.bytes)
        then:
            listener.message() == expectedMessage
        where:
            inputString      || expectedMessage
            'textMessage\n'  || 'textMessage'
            '#textMessage\n' || 'textMessage'
            '\n'             || ''
            '#\n'            || ''
    }

    @Unroll
    void 'should read encoded binary message "#inputString" from input stream and notify listener with "#expectedMessage"'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.incomingStream().addBinaryMessageListener(listener)
            channel.start()
        when:
            inputPipe.write(inputString.bytes)
        then:
            listener.message() == expectedMessage as byte[]
        where:
            inputString || expectedMessage
            '$AQID\n'   || [1, 2, 3]
            '$\n'       || []
    }

    @Unroll
    void 'should read many messages in sequence they arrived'() {
        given:
            ConsumeManyMessages listener = new ConsumeManyMessages(2)
            channel.incomingStream().addTextMessageListener(listener)
            channel.start()
        when:
            inputPipe.writeTextMessage('1')
            inputPipe.writeTextMessage('2')
        then:
            listener.messages() == ['1', '2']
    }

    void 'after stop() no new incoming messages are published to listeners'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.incomingStream().addTextMessageListener(listener)
            channel.start()
        when:
            channel.stop()
            inputPipe.writeTextMessage()
        then:
            Thread.sleep(1000) // TODO
            !listener.messageReceived()
    }

    void 'after stop() no new outgoing messages are sent'() {
        given:
            channel.start()
        when:
            channel.stop()
            channel.send('afterStop')
        then:
            Thread.sleep(1000) // TODO
            outputPipe.available() == 0
    }

    @Unroll
    void 'should notify text message listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.incomingStream().addTextMessageListener(first)
            channel.incomingStream().addTextMessageListener(last)
            channel.start()
        when:
            inputPipe.writeTextMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    @Unroll
    void 'should notify binary message listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.incomingStream().addBinaryMessageListener(first)
            channel.incomingStream().addBinaryMessageListener(last)
            channel.start()
        when:
            inputPipe.writeBinaryMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    void 'all text message listeners should be executed even when some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.incomingStream().addTextMessageListener(first)
            channel.incomingStream().addTextMessageListener(last)
            channel.start()
        when:
            inputPipe.writeTextMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }

    void 'all binary message listeners should be executed even when some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.incomingStream().addBinaryMessageListener(first)
            channel.incomingStream().addBinaryMessageListener(last)
            channel.start()
        when:
            inputPipe.writeBinaryMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }

    void 'removed text listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.incomingStream().addTextMessageListener(first)
            channel.incomingStream().addTextMessageListener(last)
            channel.start()
        when:
            channel.incomingStream().removeTextMessageListener(first)
            inputPipe.writeTextMessage()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

    void 'removed binary listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.incomingStream().addBinaryMessageListener(first)
            channel.incomingStream().addBinaryMessageListener(last)
            channel.start()
        when:
            channel.incomingStream().removeBinaryMessageListener(first)
            inputPipe.writeBinaryMessage()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
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

    void 'cant start channel when it was stopped'() {
        given:
            channel.start()
            channel.stop()
        when:
            channel.start()
        then:
            thrown(IllegalStateException)
    }

    void 'should close the channel when InputStream is closed'() {
        given:
            AwaitingConsumer stoppedListener = new AwaitingConsumer()
            channel.lifecycle().addEventListener(StoppedEvent, stoppedListener)
            channel.start()
        when:
            inputPipe.close()
        then:
            stoppedListener.waitUntilExecuted()
    }

    void 'should close the channel when OutputStream is closed'() {
        given:
            AwaitingConsumer stoppedListener = new AwaitingConsumer()
            channel.lifecycle().addEventListener(StoppedEvent, stoppedListener)
            channel.start()
        when:
            outputPipe.close()
            channel.send('a')
        then:
            stoppedListener.waitUntilExecuted()
    }

}
