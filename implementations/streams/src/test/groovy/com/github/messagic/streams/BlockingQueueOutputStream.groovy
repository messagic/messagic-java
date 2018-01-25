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

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

final class BlockingQueueOutputStream extends OutputStream {

    private final BlockingQueue<Integer> bytes = new ArrayBlockingQueue<>(1024)
    private volatile boolean closed

    @Override
    void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        bytes.put(b)
    }

    String nextLine() {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        int character
        while ((character = bytes.take()) != '\n') {
            out.write(character)
        }
        return out.toString("UTF-8") + '\n'
    }

    List<String> nextLines(int count) {
        return (1..count).collect { nextLine() }
    }

    @Override
    void close() throws IOException {
        closed = true
    }

    int available() {
        return bytes.size()
    }

}
