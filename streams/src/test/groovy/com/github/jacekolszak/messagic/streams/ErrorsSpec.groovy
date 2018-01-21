package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.Error
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout

@Timeout(5)
final class ErrorsSpec extends Specification {

    private final StreamsPipedOutputStream inputPipe = new StreamsPipedOutputStream()
    private final PipedInputStream input = inputPipe.inputStream()
    private final PipedInputStream outputPipe = new PipedInputStream()
    private final StreamsPipedOutputStream output = new StreamsPipedOutputStream(outputPipe)
    private final ConsumeOneMessage<Error> listener = new ConsumeOneMessage()

    @Subject
    private StreamsMessageChannel channel = new StreamsMessageChannel(input, output)

    void setup() {
        channel.eventBus().addListener(Error, listener)
        channel.start()
    }

    void cleanup() {
        channel.stop()
    }

    void 'should notify Error listeners about closed InputStream'() {
        when:
            inputPipe.close()
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem with decoding binary message'() {
        when:
            inputPipe.writeBinaryMessage('*')
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem during sending text message'() {
        when:
            outputPipe.close()
            channel.send('textMessage')
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem during sending binary message'() {
        when:
            outputPipe.close()
            channel.send([1, 2, 3] as byte[])
        then:
            listener.message().exception() instanceof StreamsMessageChannelException
            listener.message().channel() == channel
    }

}
