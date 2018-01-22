# Streams 

Uses Input and OutputStreams and simple text-based protocol:

```Java
MessageChannel channel = new StreamsMessageChannel(System.in, System.out);
channel.addListener(TextMessage.class, msg -> {
    ...
});
channel.start();
channel.send("Hello");
```

Can be used with stdout/stderr, TCP sockets etc. If used over TCP then netcat or socat command-line tools can be used to debug. Telnet does not work, because it uses ASCII, not UTF-8.

[More examples here](implementations/streams/src/test/java/com/github/jacekolszak/messagic/streams/examples)
