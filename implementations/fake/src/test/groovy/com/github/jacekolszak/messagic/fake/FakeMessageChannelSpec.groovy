package com.github.jacekolszak.messagic.fake

import spock.lang.Specification

final class FakeMessageChannelSpec extends Specification {

    void 'should connect fake channels'() {
        given:
            FakeMessageChannel channel1 = new FakeMessageChannel()
            FakeMessageChannel channel2 = new FakeMessageChannel()
        when:
            channel1.connect(channel2)
        then:
            // TODO
            true
    }
}
