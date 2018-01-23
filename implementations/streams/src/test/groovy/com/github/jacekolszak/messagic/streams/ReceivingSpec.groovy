package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.BinaryMessage
import com.github.jacekolszak.messagic.TextMessage
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

@Timeout(5)
final class ReceivingSpec extends Specification {

    private final BlockingQueueInputStream inputStream = new BlockingQueueInputStream()
    private final BlockingQueueOutputStream outputStream = new BlockingQueueOutputStream()

    @Subject
    private final StreamsMessageChannel channel = new StreamsMessageChannel(inputStream, outputStream)

    void cleanup() {
        channel.stop()
    }

    @Unroll
    void 'should read encoded text message "#inputString" from input stream and notify listener with "#expectedMessage"'() {
        given:
            ConsumeOneMessage<TextMessage> listener = new ConsumeOneMessage()
            channel.addListener(TextMessage, listener)
            channel.start()
        when:
            inputStream.write(inputString.getBytes("UTF-8"))
        then:
            listener.message().text() == expectedMessage
            listener.message().channel() == channel
        where:
            inputString      || expectedMessage
            'textMessage\n'  || 'textMessage'
            '#textMessage\n' || 'textMessage'
            '\n'             || ''
            '#\n'            || ''
            'Aą\n'           || 'Aą'
            'ą\n'            || 'ą' // 2 bytes in UTF-8
            'ಎ\n'            || 'ಎ' // 3 bytes
            '𐊀\n'           || '𐊀' // 4 bytes
    }

    @Unroll
    void 'should read multi-line text message "#inputStringFormatted" from input stream and notify listener with "#expectedMessageFormatted"'() {
        given:
            ConsumeOneMessage<TextMessage> listener = new ConsumeOneMessage()
            channel.addListener(TextMessage, listener)
            channel.start()
        when:
            inputStream.write(inputString.getBytes("UTF-8"))
        then:
            listener.message().text() == expectedMessage
            listener.message().channel() == channel
        where:
            inputString         || expectedMessage
            '@MULTI\nLINE\n.\n' || 'MULTI\nLINE'
            '@MULTI\n\n.\n'     || 'MULTI\n'
            '@\n.\n'            || ''
            '@\n..\n.\n'        || '\n.'
            '@\n...\n.\n'       || '\n..'
            '@@\n\n.\n'         || '@\n'
            inputStringFormatted = inputString.replaceAll('\\n', '\\\\n')
            expectedMessageFormatted = expectedMessage.replaceAll('\\n', '\\\\n')
    }

    @Unroll
    void 'should read encoded binary message "#inputString" from input stream and notify listener with "#expectedMessage"'() {
        given:
            ConsumeOneMessage<BinaryMessage> listener = new ConsumeOneMessage()
            channel.addListener(BinaryMessage, listener)
            channel.start()
        when:
            inputStream.write(inputString.getBytes("UTF-8"))
        then:
            listener.message().bytes() == expectedMessage as byte[]
            listener.message().channel() == channel
        where:
            inputString || expectedMessage
            '$AQID\n'   || [1, 2, 3]
            '$\n'       || []
    }

    @Unroll
    void 'should read many messages in sequence they arrived'() {
        given:
            ConsumeManyMessages listener = new ConsumeManyMessages(2)
            channel.addListener(TextMessage, listener)
            channel.start()
        when:
            inputStream.writeTextMessage('1')
            inputStream.writeTextMessage('2')
        then:
            listener.messages()*.text() == ['1', '2']
            listener.messages()*.channel() == [channel, channel]
    }

    void 'after stop() no new incoming messages are published to listeners'() {
        given:
            ConsumeOneMessage listener = new ConsumeOneMessage()
            channel.addListener(TextMessage, listener)
            channel.start()
        when:
            channel.stop()
            inputStream.writeTextMessage()
        then:
            Thread.sleep(1000) // TODO
            !listener.messageReceived()
    }

    @Unroll
    void 'should notify text message listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer> executionOrder = []
            AwaitingConsumer first = new AwaitingConsumer({ executionOrder << it })
            AwaitingConsumer last = new AwaitingConsumer({ executionOrder << it })
            channel.addListener(TextMessage, first)
            channel.addListener(TextMessage, last)
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
            channel.addListener(BinaryMessage, first)
            channel.addListener(BinaryMessage, last)
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
            channel.addListener(TextMessage, first)
            channel.addListener(TextMessage, last)
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
            channel.addListener(BinaryMessage, first)
            channel.addListener(BinaryMessage, last)
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
            channel.addListener(TextMessage, first)
            channel.addListener(TextMessage, last)
            channel.start()
        when:
            channel.removeListener(TextMessage, first)
            inputStream.writeTextMessage()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

    void 'removed binary listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage first = new ConsumeOneMessage()
            AwaitingConsumer last = new AwaitingConsumer()
            channel.addListener(BinaryMessage, first)
            channel.addListener(BinaryMessage, last)
            channel.start()
        when:
            channel.removeListener(BinaryMessage, first)
            inputStream.writeBinaryMessage()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

}
