# messagic-java 
Java implementation of [Messagic](https://github.com/jacekolszak/messagic), a high level API for asynchronous message passing

[![CircleCI](https://circleci.com/gh/jacekolszak/messagic-java.svg?style=svg)](https://circleci.com/gh/jacekolszak/messagic-java)

Example:

```Java
MessageChannel channel = // construct the channel using available implementation 
channel.addListener(TextMessage.class, msg -> {
    ...
});
channel.start();
channel.send("Hello");
```

## Implementations

+ [Streams](implementations/streams) - Simple text-based protocol; uses Java's Input and OutputStreams abstractions