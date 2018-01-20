package com.github.jacekolszak.messagic.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

class OutputPipe {

    private static final Logger logger = Logger.getLogger(OutputPipe.class.getName());

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream output;
    private final Limits limits;
    private final Runnable onError;

    private boolean stopped;

    OutputPipe(OutputStream output, Limits limits, Runnable onError) {
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
                        logger.log(Level.SEVERE, "Outgoing text message \"{0}...\" is bigger than allowed {1} characters", new Object[]{ textMessage.substring(0, limits.textMessageMaximumSize), limits.textMessageMaximumSize });
                        onError.run();
                        return;
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
                    if (binaryMessage.length > limits.binaryMessageMaximumSize) {
                        if (logger.isLoggable(Level.SEVERE)) {
                            String encodedMessageFragment = Base64.getEncoder().encodeToString(Arrays.copyOfRange(binaryMessage, 0, limits.binaryMessageMaximumSize));
                            logger.log(Level.SEVERE, "Outgoing binary message \"{0}...\" is bigger than allowed {1} bytes",
                                    new Object[]{ encodedMessageFragment, limits.binaryMessageMaximumSize });
                        }
                        onError.run();
                        return;
                    }
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
