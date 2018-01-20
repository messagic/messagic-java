package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

final class OutputPipe {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream output;
    private final Limits limits;
    private final Consumer<Exception> onError;

    private boolean stopped;

    OutputPipe(OutputStream output, Limits limits, Consumer<Exception> onError) {
        this.output = output;
        this.limits = limits;
        this.onError = onError;
    }

    public void send(String textMessage) {
        if (!stopped) {
            executor.submit(() -> {
                try {
                    if (messageStartsWithSpecialCharacter(textMessage)) {
                        output.write('#');
                    }
                    if (textMessage.length() > limits.textMessageMaximumSize) {
                        String error = String.format("Outgoing text message \"%s...\" is bigger than allowed %s characters", textMessage.substring(0, limits.textMessageMaximumSize), limits.textMessageMaximumSize);
                        onError.accept(new TextStreamsException(error));
                        return;
                    }
                    output.write(textMessage.getBytes());
                    output.write('\n');
                } catch (IOException e) {
                    onError.accept(new TextStreamsException("Problem writing text message to stream", e));
                }
            });
        }
    }

    private boolean messageStartsWithSpecialCharacter(String textMessage) {
        if (!textMessage.isEmpty()) {
            char firstChar = textMessage.charAt(0);
            return firstChar == '#' || firstChar == '$';
        } else {
            return false;
        }
    }

    public void send(byte[] binaryMessage) {
        if (!stopped) {
            executor.submit(() -> {
                try {
                    output.write('$');
                    if (binaryMessage.length > limits.binaryMessageMaximumSize) {
                        String encodedMessageFragment = Base64.getEncoder().encodeToString(Arrays.copyOfRange(binaryMessage, 0, limits.binaryMessageMaximumSize));
                        String error = String.format("Outgoing binary message \"%s...\" is bigger than allowed %s bytes", encodedMessageFragment, limits.binaryMessageMaximumSize);
                        onError.accept(new TextStreamsException(error));
                        return;
                    }
                    output.write(Base64.getEncoder().encode(binaryMessage));
                    output.write('\n');
                } catch (IOException e) {
                    onError.accept(new TextStreamsException("Problem writing binary message to stream", e));
                }
            });
        }
    }

    public synchronized void stop() {
        stopped = true;
        executor.shutdown();
    }

}
