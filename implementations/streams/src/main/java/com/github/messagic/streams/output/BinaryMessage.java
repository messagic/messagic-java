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
import java.util.Base64;

final class BinaryMessage {

    private final byte[] bytes;

    BinaryMessage(byte[] bytes) {
        this.bytes = bytes;
    }

    void encode(OutputStream output) throws IOException {
        output.write('$');
        output.write(Base64.getEncoder().encode(bytes));
        output.write('\n');
    }

}
