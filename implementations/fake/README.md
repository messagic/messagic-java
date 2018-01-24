# Fake 

Implementation for unit test your code. Use this implementation instead of stubbing, mocking or whatever you call it.

```Java
FakeChannel channel = new FakeChannel();
channel.addListener(TextMessage.class, msg -> {
    ...
});
channel.start();
channel.send("Hello");
```

