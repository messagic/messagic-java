# messagic-java 
Java implementation of [Messagic](https://github.com/jacekolszak/messagic), a high level API for asynchronous message passing

[![CircleCI](https://circleci.com/gh/jacekolszak/messagic-java.svg?style=svg)](https://circleci.com/gh/jacekolszak/messagic-java) [![Gitter](https://badges.gitter.im/jacekolszak/messagic.svg)](https://gitter.im/jacekolszak/messagic?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

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
