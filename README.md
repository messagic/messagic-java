# messagic-java 
Java implementation of [Messagic](https://github.com/jacekolszak/messagic), a high level API for reliable message passing

[![CircleCI](https://circleci.com/gh/jacekolszak/messagic-java.svg?style=svg)](https://circleci.com/gh/jacekolszak/messagic-java)

## Text Streams implementation - simple text-based protocol using Input and OutputStreams

```Java
MessageChannel channel = new TextStreamsMessageChannel(System.in, System.out);
channel.events().addListener(TextMessage.class, msg -> {
    ...
});
channel.start();
channel.send("Hello");
```
