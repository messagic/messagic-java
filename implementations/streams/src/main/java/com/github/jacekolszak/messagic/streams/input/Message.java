package com.github.jacekolszak.messagic.streams.input;

import java.io.IOException;

import com.github.jacekolszak.messagic.Event;

interface Message {

    void decode() throws IOException;

    Event event();

}
