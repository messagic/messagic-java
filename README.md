# messagic-java 
Java implementation of [Messagic](https://github.com/jacekolszak/messagic), a high level API for asynchronous message passing

[![CircleCI](https://circleci.com/gh/jacekolszak/messagic-java.svg?style=svg)](https://circleci.com/gh/jacekolszak/messagic-java)

## API implementations

### Streams 

Simple text-based protocol using Input and OutputStreams:

```Java
MessageChannel channel = new StreamsMessageChannel(System.in, System.out);
channel.eventBus().addListener(TextMessage.class, msg -> {
    ...
});
channel.start();
channel.send("Hello");
```

Can be used with stdout/stderr, TCP sockets etc. If used over TCP then netcat or socat command-line tools can be used to debug. Telnet does not work, because it uses ASCII, not UTF-8.

[More examples here](implementations/streams/src/test/java/com/github/jacekolszak/messagic/streams/examples)
