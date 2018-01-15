package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.FatalError
import com.github.jacekolszak.messagic.MessageChannel
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@Timeout(3)
class StreamsChannelSpec extends Specification {

    private final PipedInputStream input = new PipedInputStream()
    private final PipedOutputStream inputPipe = new PipedOutputStream(input)
    private final PipedInputStream outputPipe = new PipedInputStream()
    private final PipedOutputStream output = new PipedOutputStream(outputPipe)
    private final InputStreamReader outputReader = new InputStreamReader(outputPipe)

    @Subject
    private final MessageChannel channel = Streams.channel(input, output)

    void 'should create channel'() {
        expect:
            channel != null
    }

    void 'should send text message to stream'() {
        when:
            channel.send('textMessage')
        then:
            outputReader.readLine() == 'textMessage'
    }

    void 'should send empty text message to stream'() {
        when:
            channel.send('')
        then:
            outputReader.readLine() == ''
    }

    @Unroll
    void 'when send is executed, should encode text message "#message" as "#line\\n"'() {
        when:
            channel.send(message)
        then:
            outputReader.readLine() == line
        where:
            message    || line
            '#message' || '##message'
            '$message' || '#$message'
            '!message' || '#!message'
    }

    void 'binary messages should be encoded using base64 with "$" character as a prefix and new line in the end'() {
        when:
            channel.send([1, 2, 3] as byte[])
        then:
            outputReader.readLine() == '$AQID'
    }

    @Unroll
    void 'should parse encoded text message "#inputString" and pass it to consumer as "#expectedMessage"'() {
        given:
            CountDownLatch latch = new CountDownLatch(1)
            String messageReceived = null
            channel.textMessageConsumer = { msg ->
                messageReceived = msg
                latch.countDown()
            }
            channel.open()
        when:
            inputPipe.write(inputString.bytes)
        then:
            latch.await(2, TimeUnit.SECONDS)
            messageReceived == expectedMessage
        where:
            inputString      || expectedMessage
            'textMessage\n'  || 'textMessage'
            '#textMessage\n' || 'textMessage'
            '\n'             || ''
            '#\n'            || ''
    }

    void 'should parse encoded binary message and pass it to consumer'() {
        given:
            CountDownLatch latch = new CountDownLatch(1)
            byte[] messageReceived = null
            channel.binaryMessageConsumer = { msg ->
                messageReceived = msg
                latch.countDown()
            }
            channel.open()
        when:
            inputPipe.write('$AQID\n'.bytes) // #123\n
        then:
            latch.await(2, TimeUnit.SECONDS)
            messageReceived == [1, 2, 3] as byte[]
    }

    void 'should send error to sender when binary message cannot be parsed'() {
        given:
            channel.binaryMessageConsumer = {}
            channel.open()
        when:
            inputPipe.write('$@$%\n'.bytes)
        then:
            outputReader.readLine().startsWith('!Bad encoding of incoming binary message: ')
    }

    void 'should send error to sender when binary message was sent but no consumer was set on the receiver'() {
        given:
            channel.open()
        when:
            inputPipe.write('$AQID\n'.bytes) // #123\n
        then:
            outputReader.readLine() == '!Binary message cannot be consumed'
    }

    void 'should send error to sender when text message was sent but no consumer was set on the receiver'() {
        given:
            channel.open()
        when:
            inputPipe.write('text\n'.bytes)
        then:
            outputReader.readLine() == '!Text message cannot be consumed'
    }

    void 'should ignore error messages sent by other peer when error consumer is not set'() {
        given:
            channel.textMessageConsumer = { msg -> channel.send('ok') }
            channel.open()
        when:
            inputPipe.write('!error\n'.bytes)
            inputPipe.write('is ok?\n'.bytes)
        then:
            outputReader.readLine() == 'ok'
    }

    @Unroll
    void 'should send "#line" when consumer throws exception with message "#exceptionMessage"'() {
        given:
            CountDownLatch latch = new CountDownLatch(1)
            channel.textMessageConsumer = { msg ->
                if (msg == 'messageCausingError') {
                    throw new RuntimeException(exceptionMessage)
                } else {
                    latch.countDown()
                }
            }
            channel.open()
        when:
            inputPipe.write('messageCausingError\n'.bytes)
            inputPipe.write('other\n'.bytes)
        then:
            latch.await(2, TimeUnit.SECONDS)
            outputReader.readLine() == line
        where:
            exceptionMessage       || line
            'Deliberate exception' || '!Deliberate exception'
            ''                     || '!'
    }

    void 'should send !null when consumer throws exception with null message'() {
        given:
            CountDownLatch latch = new CountDownLatch(1)
            channel.textMessageConsumer = { msg ->
                if (msg == 'messageCausingError') {
                    throw new RuntimeException((String) null)
                } else {
                    latch.countDown()
                }
            }
            channel.open()
        when:
            inputPipe.write('messageCausingError\n'.bytes)
            inputPipe.write('other\n'.bytes)
        then:
            latch.await(2, TimeUnit.SECONDS)
            outputReader.readLine() == '!null'
    }

    void 'should limit number of characters for error message to errorMessageCutOffSize'() {
        given:
            CountDownLatch latch = new CountDownLatch(1)
            channel.errorMessageCutOffSize = 4;
            channel.textMessageConsumer = { msg ->
                if (msg == 'messageCausingError') {
                    throw new RuntimeException('very long message')
                } else {
                    latch.countDown()
                }
            }
            channel.open()
        when:
            inputPipe.write('messageCausingError\n'.bytes)
            inputPipe.write('other\n'.bytes)
        then:
            latch.await(2, TimeUnit.SECONDS)
            outputReader.readLine() == '!very'
    }

    void 'should parse error'() {
        given:
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            inputPipe.write('!Some error\n'.bytes)
        then:
            errorConsumer.await()
            FatalError errorReceived = errorConsumer.errorReceived
            errorReceived.isPeerError()
            errorReceived.message() == 'Some error'
            !errorReceived.isPeerNotReachable()
    }

    void 'when could not read from input stream then PeerNotReachable error should be reported'() {
        given:
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            inputPipe.close()
        then:
            errorConsumer.await()
            errorConsumer.errorReceived.isPeerNotReachable()
    }

    void 'when could not write text to output stream then PeerNotReachable error should be reported'() {
        given:
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            outputPipe.close()
            channel.send('test')
        then:
            errorConsumer.await()
            errorConsumer.errorReceived.isPeerNotReachable()
    }

    void 'when could not write binary to output stream then PeerNotReachable error should be reported'() {
        given:
            PipedOutputStream out = new PipedOutputStream()
            PipedInputStream outputPipe = new PipedInputStream(out)
            MessageChannel channel = Streams.channel(input, out)
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            outputPipe.close()
            channel.send([1] as byte[])
        then:
            errorConsumer.await()
            errorConsumer.errorReceived.isPeerNotReachable()
    }

    void 'should report an error and close the channel when sent binary message was too big'() {
        given:
            channel.binaryMessageMaximumSize = 16
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            channel.send(new byte[17])
        then:
            errorConsumer.await()
            FatalError errorReceived = errorConsumer.errorReceived
            errorReceived.isPeerNotReachable()
            errorReceived.message() == 'Payload of sent binary message exceeded maximum size'
    }

    void 'should report an error and close the channel when sent text message was too big'() {
        given:
            channel.textMessageMaximumSize = 5
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            channel.send('123456')
        then:
            errorConsumer.await()
            FatalError errorReceived = errorConsumer.errorReceived
            errorReceived.isPeerNotReachable()
            errorReceived.message() == 'Payload of sent text message exceeded maximum size'
    }

    void 'should report and close the channel when received binary message was too big'() {
        given:
            channel.binaryMessageMaximumSize = 2
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            inputPipe.write('$AQID\n'.bytes) // 3 bytes that is [1,2,3]
        then:
            errorConsumer.await()
            FatalError errorReceived = errorConsumer.errorReceived
            errorReceived.isPeerNotReachable()
            errorReceived.message() == 'Payload of received binary message exceeded maximum size'
    }

    void 'should report and close the channel when received text message was too big'() {
        given:
            channel.textMessageMaximumSize = 2
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            inputPipe.write('123\n'.bytes)
        then:
            errorConsumer.await()
            FatalError errorReceived = errorConsumer.errorReceived
            errorReceived.isPeerNotReachable()
            errorReceived.message() == 'Payload of received text message exceeded maximum size'
    }

    void 'should report and close the channel when received error message was too big'() {
        given:
            channel.textMessageMaximumSize = 2
            ErrorConsumerMock errorConsumer = new ErrorConsumerMock()
            channel.errorConsumer = errorConsumer
            channel.open()
        when:
            inputPipe.write('!123\n'.bytes)
        then:
            errorConsumer.await()
            FatalError errorReceived = errorConsumer.errorReceived
            errorReceived.isPeerNotReachable()
            errorReceived.message() == 'Payload of received error message exceeded maximum size'
    }

    private class ErrorConsumerMock implements Consumer<FatalError> {

        private FatalError errorReceived = null
        private final CountDownLatch latch

        private ErrorConsumerMock() {
            this.latch = new CountDownLatch(1)
        }

        @Override
        void accept(FatalError fatalError) {
            errorReceived = fatalError
            latch.countDown()
        }

        boolean await() {
            latch.await(2, TimeUnit.SECONDS)
        }

    }

}
