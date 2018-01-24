# Fake 

Implementation for unit test your code. Use this implementation instead of stubbing, mocking or however you call it:

```Java
FakeMessageChannel channel = new FakeMessageChannel();
channel.addListener(TextMessage.class, msg -> {
    ...
});
channel.start();
channel.send("Hello");
```

