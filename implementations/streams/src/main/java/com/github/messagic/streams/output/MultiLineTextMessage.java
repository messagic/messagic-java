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
package com.github.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;

final class MultiLineTextMessage implements TextMessage {

    private final String text;

    MultiLineTextMessage(String text) {
        this.text = text;
    }

    @Override
    public void encode(OutputStream output) throws IOException {
        output.write('@');
        String escapedText = text.replace("\n.", "\n..");
        output.write(escapedText.getBytes("UTF-8"));
        output.write('\n');
        output.write('.');
        output.write('\n');
    }

    private boolean messageStartsWithSpecialCharacter() {
        if (!text.isEmpty()) {
            char firstChar = text.charAt(0);
            return firstChar == '#' || firstChar == '$' || firstChar == '@';
        } else {
            return false;
        }
    }

}
