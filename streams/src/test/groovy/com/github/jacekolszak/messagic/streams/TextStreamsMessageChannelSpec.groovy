package com.github.jacekolszak.messagic.streams

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

@Timeout(15)
class TextStreamsMessageChannelSpec extends Specification {

    private final PipedInputStream input = new PipedInputStream()
    private final PipedOutputStream inputPipe = new PipedOutputStream(input)
    private final PipedInputStream outputPipe = new PipedInputStream()
    private final PipedOutputStream output = new PipedOutputStream(outputPipe)
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
            ConsumeOneMessage<String> listener = new ConsumeOneMessage<>()
            channel.messageListeners().addTextMessageListener(listener)
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
            ConsumeOneMessage<byte[]> listener = new ConsumeOneMessage<>()
            channel.messageListeners().addBinaryMessageListener(listener)
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
    void 'should notify text message listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer<String>> executionOrder = []
            AwaitingConsumer<String> first = new AwaitingConsumer<>({ executionOrder << it })
            AwaitingConsumer<String> last = new AwaitingConsumer<>({ executionOrder << it })
            channel.messageListeners().addTextMessageListener(first)
            channel.messageListeners().addTextMessageListener(last)
            channel.start()
        when:
            writeTextMessageToInputPipe()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    private writeTextMessageToInputPipe() {
        inputPipe.write('message\n'.bytes)
    }

    @Unroll
    void 'should notify binary message listeners in sequence based on the order they were registered'() {
        given:
            List<AwaitingConsumer<byte[]>> executionOrder = []
            AwaitingConsumer<byte[]> first = new AwaitingConsumer<>({ executionOrder << it })
            AwaitingConsumer<byte[]> last = new AwaitingConsumer<>({ executionOrder << it })
            channel.messageListeners().addBinaryMessageListener(first)
            channel.messageListeners().addBinaryMessageListener(last)
            channel.start()
        when:
            writeBinaryMessageToInputPipe()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
            executionOrder == [first, last]
    }

    private writeBinaryMessageToInputPipe() {
        inputPipe.write('$AQID\n'.bytes)
    }

    void 'all text message listeners should be executed even when some listener thrown exception'() {
        given:
            AwaitingConsumer<String> first = new AwaitingConsumer<>({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer<String> last = new AwaitingConsumer<>({})
            channel.messageListeners().addTextMessageListener(first)
            channel.messageListeners().addTextMessageListener(last)
            channel.start()
        when:
            writeTextMessageToInputPipe()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }

    void 'all binary message listeners should be executed even when some listener thrown exception'() {
        given:
            AwaitingConsumer<byte[]> first = new AwaitingConsumer<>({ throw new RuntimeException('Deliberate exception') })
            AwaitingConsumer<byte[]> last = new AwaitingConsumer<>({})
            channel.messageListeners().addBinaryMessageListener(first)
            channel.messageListeners().addBinaryMessageListener(last)
            channel.start()
        when:
            writeBinaryMessageToInputPipe()
        then:
            first.waitUntilExecuted()
            last.waitUntilExecuted()
    }

    void 'removed text listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage<String> first = new ConsumeOneMessage<>()
            AwaitingConsumer<String> last = new AwaitingConsumer<>({})
            channel.messageListeners().addTextMessageListener(first)
            channel.messageListeners().addTextMessageListener(last)
            channel.start()
        when:
            channel.messageListeners().removeTextMessageListener(first)
            writeTextMessageToInputPipe()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }

    void 'removed binary listeners does not receive notifications anymore'() {
        given:
            ConsumeOneMessage<byte[]> first = new ConsumeOneMessage<>()
            AwaitingConsumer<byte[]> last = new AwaitingConsumer<>({})
            channel.messageListeners().addBinaryMessageListener(first)
            channel.messageListeners().addBinaryMessageListener(last)
            channel.start()
        when:
            channel.messageListeners().removeBinaryMessageListener(first)
            writeBinaryMessageToInputPipe()
        then:
            last.waitUntilExecuted()
            !first.messageReceived()
    }


}
