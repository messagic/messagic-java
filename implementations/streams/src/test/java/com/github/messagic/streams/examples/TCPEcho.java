package com.github.messagic.streams.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.github.messagic.TextMessage;
import com.github.messagic.streams.StreamsMessageChannel;

final class TCPEcho {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        // following piece of code is the same as in StandardIOEcho:
        StreamsMessageChannel channel = new StreamsMessageChannel(inputStream, outputStream);
        channel.addListener(TextMessage.class, e -> channel.send(e.text()));
        channel.start();
    }

}
