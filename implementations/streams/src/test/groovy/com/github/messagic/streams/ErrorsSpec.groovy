/*
 * Copyright 2018 The Messagic Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.messagic.streams

import com.github.messagic.Error
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
        channel.addListener(Error, listener)
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
