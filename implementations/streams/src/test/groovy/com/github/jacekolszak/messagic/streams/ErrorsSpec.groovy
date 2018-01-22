package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.Error
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout

@Timeout(5)
final class ErrorsSpec extends Specification {

    private final BlockingQueueInputStream inputStream = new BlockingQueueInputStream()
    private final BlockingQueueOutputStream outputStream = new BlockingQueueOutputStream()
    private final ConsumeOneMessage<Error> listener = new ConsumeOneMessage()

    @Subject
    private StreamsMessageChannel channel = new StreamsMessageChannel(inputStream, outputStream)

    void setup() {
        channel.eventBus().addListener(Error, listener)
        channel.start()
    }

    void cleanup() {
        channel.stop()
    }

    void 'should notify Error listeners about closed InputStream'() {
        when:
            inputStream.close()
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem with decoding binary message'() {
        when:
            inputStream.writeBinaryMessage('*')
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem with reading InputStream'() {
        when:
            inputStream.readThrowsException()
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem during sending text message'() {
        when:
            outputStream.close()
            channel.send('textMessage')
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem during sending binary message'() {
        when:
            outputStream.close()
            channel.send([1, 2, 3] as byte[])
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

}
