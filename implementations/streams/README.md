# Streams 

Uses Java's Input and OutputStreams and simple text-based protocol:

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

## Protocol

+ text-based - text messages are encoded as is, binary ones using [Base64](https://en.wikipedia.org/wiki/Base64) encoding (mostly for debugging purposes)
+ uses UTF-8 encoding (always)
+ every message is a line (sequence of characters with EOL "\n" on the end)
+ text message is encoded as is or with "#" character prefix, e.g.:

  ```some message\n``` decodes to ```some message```
  
  ```#some message\n``` decodes to ```some message```
  
  ```##some message\n``` decodes to ```#some message```

+ binary message is encoded using [Base64](https://en.wikipedia.org/wiki/Base64) always with "$" character prefix, e.g.:

  ```$AQID\n``` decodes to ```[1,2,3]```
  
+ multi-line text message is encoded with "@" character prefix. EOL is no longer a message end. Line ".\n" should be used instead

  ```
     @multi-line\n
     text message\n
     .\n
  ```
  
  decodes to
  
  ```multi-line\ntext message```