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
package com.github.messagic.streams;

import java.io.InputStream;

import com.github.messagic.MessageChannel;
import com.github.messagic.streams.input.MessageStream;
import com.github.messagic.streams.output.MessageFactory;

public final class Limits {

    private int textMessageMaximumSize = 4096;
    private int binaryMessageMaximumSize = 4096;

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param bytes Number of bytes. Default is 4096
     */
    public void setBinaryMessageMaximumSize(int bytes) {
        this.binaryMessageMaximumSize = bytes;
    }

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param characters Number of characters. Default is 4096
     */
    public void setTextMessageMaximumSize(int characters) {
        this.textMessageMaximumSize = characters;
    }

    MessageFactory messageFactory() {
        return new MessageFactory(textMessageMaximumSize, binaryMessageMaximumSize);
    }

    MessageStream messageStream(InputStream inputStream, MessageChannel channel) {
        return new MessageStream(inputStream, channel, textMessageMaximumSize, binaryMessageMaximumSize);
    }

}
