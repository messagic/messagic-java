package com.github.jacekolszak.messagic.streams;

import com.github.jacekolszak.messagic.FatalError;

class EndpointNotReachable implements FatalError {

    private String message;

    EndpointNotReachable(String message) {
        this.message = message;
    }

    @Override
    public boolean isPeerError() {
        return false;
    }

    @Override
    public String message() {
        return message;
    }

}
