# messagic-java 
Java implementation of [Messagic](https://github.com/jacekolszak/messagic), a high level API for reliable message passing

[![CircleCI](https://circleci.com/gh/jacekolszak/messagic-java.svg?style=svg)](https://circleci.com/gh/jacekolszak/messagic-java)

## Streams implementation

```Java
MessageChannel channel = Streams.channel(System.in, System.out);
channel.setTextMessageConsumer( msg -> {
    ...
});
channel.open();
channel.send("Hello");
```