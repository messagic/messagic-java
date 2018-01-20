package com.github.jacekolszak.messagic.streams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ChannelDispatchThread {

    private ExecutorService executor;

    void start() {
        executor = Executors.newSingleThreadExecutor();
    }

    void execute(Runnable code) {
        executor.execute(code);
    }

    void stop() {
        executor.shutdown();
    }

}
