package com.github.jacekolszak.messagic.streams;

public final class Limits {

    int binaryMessageMaximumSize = 4096;
    int textMessageMaximumSize = 4096;

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param bytes Number of bytes. Default is 4096
     */
    public void setBinaryMessageMaximumSize(int bytes) {
        this.binaryMessageMaximumSize = bytes;
    }

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param characters Number of characters. Default is 4096
     */
    public void setTextMessageMaximumSize(int characters) {
        this.textMessageMaximumSize = characters;
    }

}
