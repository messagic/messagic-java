package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.BinaryMessage
import com.github.jacekolszak.messagic.TextMessage
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

@Timeout(5)
final class StreamsMessageChannelSpec extends Specification {

    private final BlockingQueueInputStream inputStream = new BlockingQueueInputStream()
    private final BlockingQueueOutputStream outputStream = new BlockingQueueOutputStream()

    @Subject
    private final StreamsMessageChannel channel = new StreamsMessageChannel(inputStream, outputStream)

    void cleanup() {
        channel.stop()
    }

    void 'should send text message to output stream'() {
        given:
            channel.start()
        when:
            channel.send('textMessage')
        then:
            outputStream.nextLine() == 'textMessage'
    }

    void 'should send binary message to output stream'() {
        given:
            channel.start()
        when:
            channel.send([1, 2, 3] as byte[])
        then:
            outputStream.nextLine() == '$AQID'
    }

    void 'sending text message should be asynchronous'() {
        given:
            StreamsMessageChannel channel = new StreamsMessageChannel(inputStream, new ThreadBlockingOutputStream())
            channel.start()
        when:
            channel.send('textMessage')
        then:
            true
    }

    void 'sending binary message should be asynchronous'() {
        given:
            StreamsMessageChannel channel = new StreamsMessageChannel(inputStream, new ThreadBlockingOutputStream())
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
            outputStream.nextLine() == ''
    }

    void 'should send empty binary message to output stream'() {
        given:
            channel.start()
        when:
            channel.send(new byte[0])
        then:
            outputStream.nextLine() == '$'
    }

    @Unroll
    void 'should send text message "#message" encoded as "#line\\n"'() {
        given:
            channel.start()
        when:
            channel.send(message)
        then:
            outputStream.nextLine() == line
        where:
            message    || line
            '#message' || '##message'
            '$message' || '#$message'
    }

    @Unroll
    void 'should read encoded text message "#inputString" from input stream and notify listener with "#expectedMessage"'() {
        given:
            ConsumeOneMessage<TextMessage> listener = new ConsumeOneMessage()
            channel.eventBus().addListener(TextMessage, listener)
            channel.start()
        when:
            inputStream.write(inputString.bytes)
        then:
            listener.message().text() == expectedMessage
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
            ConsumeOneMessage<BinaryMessage> listener = new ConsumeOneMessage()
            channel.eventBus().addListener(BinaryMessage, listener)
            channel.start()
        when:
            inputStream.write(inputString.bytes)
        then:
            listener.message().bytes() == expectedMessage as byte[]
        where:
            inputString || expectedMessage
            '$AQID\n'   || [1, 2, 3]
            '$\n'       || []
    }

    @Unroll
    void 'should read many messages in sequence they arrived'() {
        given:
            ConsumeManyMessages listener = new ConsumeManyMessages(2)
            channel.eventBus().addListener(TextMessage, listener)
            channel.start()
        when:
            inputStream.writeTextMessage('1')
            inputStream.writeTextMessage('2')
        then:
            listener.messages()*.text() == ['1', '2']
    }

    void 'after stop() no new incoming messages are published to listeners'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.eventBus().addListener(TextMessage, listener)
            channel.start()
        when:
            channel.stop()
            inputStream.writeTextMessage()
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
            outputStream.available() == 0
    }

    @Unroll
    void 'should notify text message listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.eventBus().addListener(TextMessage, first)
            channel.eventBus().addListener(TextMessage, last)
            channel.start()
        when:
            inputStream.writeTextMessage()
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
            channel.eventBus().addListener(BinaryMessage, first)
            channel.eventBus().addListener(BinaryMessage, last)
            channel.start()
        when:
            inputStream.writeBinaryMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    void 'all text message listeners should be executed even when some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.eventBus().addListener(TextMessage, first)
            channel.eventBus().addListener(TextMessage, last)
            channel.start()
        when:
            inputStream.writeTextMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }

    void 'all binary message listeners should be executed even when some listener thrown exception'() {
        given:
            AwaitingConsumer first = new AwaitingConsumer({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer last = new AwaitingConsumer()
            channel.eventBus().addListener(BinaryMessage, first)
            channel.eventBus().addListener(BinaryMessage, last)
            channel.start()
        when:
            inputStream.writeBinaryMessage()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }

    void 'removed text listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.eventBus().addListener(TextMessage, first)
            channel.eventBus().addListener(TextMessage, last)
            channel.start()
        when:
            channel.eventBus().removeListener(TextMessage, first)
            inputStream.writeTextMessage()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

    void 'removed binary listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.eventBus().addListener(BinaryMessage, first)
            channel.eventBus().addListener(BinaryMessage, last)
            channel.start()
        when:
            channel.eventBus().removeListener(BinaryMessage, first)
            inputStream.writeBinaryMessage()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

}
