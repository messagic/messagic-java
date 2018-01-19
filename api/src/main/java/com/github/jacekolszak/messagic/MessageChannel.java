package com.github.jacekolszak.messagic;

/***
 * Please note that sending, starting and stopping channel is asynchronous, ie. does not block the current thread until it is done.
 *
 * If implementation of this class can't send the message then channel is closed (and StoppedEvent is published)
 *
 */
public interface MessageChannel {

    Lifecycle lifecycle();

    MessageListeners messageListeners();

    /**
     * Once started channel allows to send messages and accepts incoming ones. After successful start StartedEvent is published.
     */
    void start();

    void send(String textMessage);

    void send(byte[] binaryMessage);

    /**
     * Once stopped no new messages can be send and no incoming messages will be read. Implementation may close the socket, stream or whatever.
     * After successful stop StoppedEvent is published.
     */
    void stop();

}
