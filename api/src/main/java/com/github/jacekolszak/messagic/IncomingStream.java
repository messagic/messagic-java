package com.github.jacekolszak.messagic;

import java.util.function.Consumer;

public interface IncomingStream {

    void addTextMessageListener(Consumer<String> listener);

    void removeTextMessageListener(Consumer<String> listener);

    void addBinaryMessageListener(Consumer<byte[]> listener);

    void removeBinaryMessageListener(Consumer<byte[]> listener);

}
