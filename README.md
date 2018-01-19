# messagic-java 
Java implementation of [Messagic](https://github.com/jacekolszak/messagic), a high level API for reliable message passing

[![CircleCI](https://circleci.com/gh/jacekolszak/messagic-java.svg?style=svg)](https://circleci.com/gh/jacekolszak/messagic-java)

## Streams implementation

```Java
MessageChannel channel = new TextStreamsMessageChannel(System.in, System.out);
channel.messageListeners().addTextMessageListener(msg -> {
    ...
});
channel.start();
channel.send("Hello");
```
