package com.github.messagic.streams.output;

import java.io.IOException;
import java.io.OutputStream;

interface TextMessage {

    void encode(OutputStream output) throws IOException;
}
