package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class OutputPipe {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream output;

    OutputPipe(OutputStream output) {
        this.output = output;
    }

    public void send(String textMessage) {
        executor.submit(() -> {
            try {
                if (messageStartsWithSpecialCharacter(textMessage)) {
                    output.write('#');
                }
                output.write(textMessage.getBytes());
                output.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        executor.submit(() -> {
            try {
                output.write('$');
                output.write(Base64.getEncoder().encode(binaryMessage));
                output.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
