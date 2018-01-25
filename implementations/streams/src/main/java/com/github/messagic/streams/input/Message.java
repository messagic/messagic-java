package com.github.messagic.streams.input;

import java.io.IOException;

import com.github.messagic.Event;

interface Message {

    Event event() throws IOException;

}
