package com.github.jacekolszak.messagic.streams

import com.github.jacekolszak.messagic.Error
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout

@Timeout(5)
final class TextStreamsMessageChannelErrorsSpec extends Specification {

    private final TextStreamsPipedOutputStream inputPipe = new TextStreamsPipedOutputStream()
    private final PipedInputStream input = inputPipe.inputStream()
    private final PipedInputStream outputPipe = new PipedInputStream()
    private final TextStreamsPipedOutputStream output = new TextStreamsPipedOutputStream(outputPipe)
    private final ConsumeOneMessage<Error> listener = new ConsumeOneMessage()

    @Subject
    private TextStreamsMessageChannel channel = new TextStreamsMessageChannel(input, output)

    void setup() {
        channel.events().addListener(Error, listener)
        channel.start()
    }

    void cleanup() {
        channel.stop()
    }

    void 'should notify Error listeners about closed InputStream'() {
        when:
            inputPipe.close()
        then:
            listener.message().exception() instanceof TextStreamsException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem with decoding binary message'() {
        when:
            inputPipe.writeBinaryMessage('*')
        then:
            listener.message().exception() instanceof TextStreamsException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem during sending text message'() {
        when:
            outputPipe.close()
            channel.send('textMessage')
        then:
            listener.message().exception() instanceof TextStreamsException
            listener.message().channel() == channel
    }

    void 'should notify Error listeners about problem during sending binary message'() {
        when:
            outputPipe.close()
            channel.send([1, 2, 3] as byte[])
        then:
            listener.message().exception() instanceof TextStreamsException
            listener.message().channel() == channel
    }

}
