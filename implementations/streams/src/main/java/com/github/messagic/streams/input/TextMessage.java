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
package com.github.messagic.streams.input;

import com.github.messagic.Event;
import com.github.messagic.MessageChannel;
import com.github.messagic.streams.StreamsMessageChannelException;

final class TextMessage implements Message {

    private final String encodedMessage;
    private final MessageChannel channel;
    private final int textMessageMaximumSize;

    TextMessage(MessageChannel channel, String encodedMessage, int textMessageMaximumSize) {
        this.encodedMessage = encodedMessage;
        this.channel = channel;
        this.textMessageMaximumSize = textMessageMaximumSize;
    }

    @Override
    public Event event() throws StreamsMessageChannelException {
        if (encodedMessage.length() > textMessageMaximumSize) {
            String error = String.format("Incoming text message \"%s...\" is bigger than allowed %s characters",
                    encodedMessage.substring(0, textMessageMaximumSize), textMessageMaximumSize);
            throw new StreamsMessageChannelException(error);
        }
        return new TextMessageEvent(channel, encodedMessage);
    }

}
