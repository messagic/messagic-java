package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;

interface Message {

    DecodedMessage decodedMessage() throws IOException;

}
