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

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Timeout
import spock.lang.Unroll

@Timeout(5)
final class SendingSpec extends Specification {

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
            outputStream.nextLine() == 'textMessage\n'
    }

    void 'should send binary message to output stream'() {
        given:
            channel.start()
        when:
            channel.send([1, 2, 3] as byte[])
        then:
            outputStream.nextLine() == '$AQID\n'
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
            outputStream.nextLine() == '\n'
    }

    void 'should send empty binary message to output stream'() {
        given:
            channel.start()
        when:
            channel.send(new byte[0])
        then:
            outputStream.nextLine() == '$\n'
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
            '#message' || '##message\n'
            '$message' || '#$message\n'
            '@message' || '#@message\n'
            '.'        || '.\n'
    }

    @Unroll
    void 'should send multi-line text message "#messageFormatted" encoded as "#encodedFormatted"'() {
        given:
            channel.start()
        when:
            channel.send(message)
        then:
            outputStream.nextLines(3).join() == encoded
        where:
            message       || encoded
            'MULTI\nLINE' || '@MULTI\nLINE\n.\n'
            'MULTI\n'     || '@MULTI\n\n.\n'
            '\n'          || '@\n\n.\n'
            '\n.'         || '@\n..\n.\n'
            '\n..'        || '@\n...\n.\n'
            '@\n'         || '@@\n\n.\n'
            messageFormatted = message.replaceAll('\\n', '\\\\n')
            encodedFormatted = encoded.replaceAll('\\n', '\\\\n')
    }

    void 'after stop() no new outgoing messages are sent'() {
        given:
            channel.start()
        when:
            channel.stop()
            channel.send('afterStop')
        then:
            Thread.sleep(500) // TODO
            outputStream.available() == 0
    }

}
