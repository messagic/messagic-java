package com.github.jacekolszak.messagic.streams;

public class Limits {

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param bytes Number of bytes. Default is 4096
     */
    public void setBinaryMessageMaximumSize(int bytes) {

    }

    /**
     * Exceeding the maximum size either during pushing messages or receiving them will stop the
     * channel and report error
     *
     * @param characters Number of characters. Default is 4096
     */
    public void setTextMessageMaximumSize(int characters) {

    }

}
