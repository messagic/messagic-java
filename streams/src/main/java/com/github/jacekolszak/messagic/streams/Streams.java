package com.github.jacekolszak.messagic.streams;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jacekolszak.messagic.MessageChannel;

/**
 * Encodes messages into OutputStream and decodes from InputStream.
 * <p>
 * The implementation is very simple - every message is separated by a new line character. First byte in the message is a message type. Rest is the payload, ex.
 * <ul>
 * <li><code>#textMessage\n</code> ex. <code>#hello\n</code></li>
 * <li><code>$binaryMessageEncodedInBase64\n</code> ex. <code>$AQID</code> for bytes [1,2,3]</li>
 * <li><code>!error\n</code>, ex. <code>!something bad happened</code></li>
 * </ul>
 * Message type can be skipped for text messages if the message does not start with <code>#</code>, <code>$</code> or <code>!</code> ex. <code>hello\n</code>
 */
public final class Streams {

    public static MessageChannel channel(InputStream input, OutputStream output) {
        return new StreamsMessageChannel(input, output);
    }

}
