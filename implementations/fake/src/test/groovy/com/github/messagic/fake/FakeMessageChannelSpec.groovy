package com.github.messagic.fake

import com.github.messagic.BinaryMessage
import com.github.messagic.Started
import com.github.messagic.Stopped
import com.github.messagic.TextMessage
import spock.lang.Specification
import spock.lang.Timeout

@Timeout(5)
final class FakeMessageChannelSpec extends Specification {

    private final FakeMessageChannel channel1 = new FakeMessageChannel()
    private final FakeMessageChannel channel2 = new FakeMessageChannel()
    private final ConsumeOneMessage<Started> startedListener1 = new ConsumeOneMessage<>()
    private final ConsumeOneMessage<Started> startedListener2 = new ConsumeOneMessage<>()
    private final ConsumeOneMessage<Stopped> stoppedListener1 = new ConsumeOneMessage<>()
    private final ConsumeOneMessage<Stopped> stoppedListener2 = new ConsumeOneMessage<>()
    private final ConsumeOneMessage<TextMessage> textMessageListener = new ConsumeOneMessage<>()
    private final ConsumeOneMessage<BinaryMessage> binaryMessageListener = new ConsumeOneMessage<>()

    void cleanup() {
        channel1.stop()
        channel2.stop()
    }

    void 'should notify listeners with Started event after channels are connected'() {
        given:
            channel1.addListener(Started, startedListener1)
            channel2.addListener(Started, startedListener2)
            channel1.start()
            channel2.start()
        when:
            channel1.connect(channel2)
        then:
            startedListener1.message().channel() == channel1
            startedListener2.message().channel() == channel2
    }

    void 'should send text message to connected channel'() {
        given:
            channel1.start()
            channel2.start()
            channel1.connect(channel2)
            channel2.addListener(TextMessage, textMessageListener)
        when:
            channel1.send('message')
        then:
            textMessageListener.message().text() == 'message'
            textMessageListener.message().channel() == channel2
    }

    void 'should send binary message to connected channel'() {
        given:
            channel1.start()
            channel2.start()
            channel1.connect(channel2)
            channel2.addListener(BinaryMessage, binaryMessageListener)
        when:
            channel1.send([1, 2, 3] as byte[])
        then:
            binaryMessageListener.message().bytes() == [1, 2, 3] as byte[]
            binaryMessageListener.message().channel() == channel2
    }

    void 'should notify listeners with Stopped event after one channel is stopped'() {
        given:
            channel1.addListener(Stopped, stoppedListener1)
            channel2.addListener(Stopped, stoppedListener2)
            channel1.start()
            channel2.start()
            channel1.connect(channel2)
        when:
            channel1.stop()
        then:
            stoppedListener1.message().channel() == channel1
            stoppedListener2.message().channel() == channel2
    }

}
