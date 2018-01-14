package com.github.jacekolszak.messagic;

public interface FatalError {

    boolean isPeerError();

    default boolean isPeerNotReachable() {
        return !isPeerError();
    }

    String message();

}


