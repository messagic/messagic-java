package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

class OutputPipe {

    private static final Logger logger = Logger.getLogger(OutputPipe.class.getName());

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream output;
    private final Runnable onError;

    private boolean stopped;

    OutputPipe(OutputStream output, Runnable onError) {
        this.output = output;
        this.onError = onError;
    }

    public void send(String textMessage) {
        if (!stopped) {
            executor.submit(() -> {
                try {
                    if (messageStartsWithSpecialCharacter(textMessage)) {
                        output.write('#');
                    }
                    output.write(textMessage.getBytes());
                    output.write('\n');
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Problem writing text message to stream", e);
                    onError.run();
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
                    output.write(Base64.getEncoder().encode(binaryMessage));
                    output.write('\n');
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Problem writing binary message to stream", e);
                    onError.run();
                }
            });
        }
    }

    public synchronized void stop() {
        stopped = true;
        executor.shutdown();
    }

}
