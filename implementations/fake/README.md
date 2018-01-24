# Fake 

Implementation for unit test your code. Use this implementation instead of stubbing, mocking or however you call it:

```Java
FakeMessageChannel channel1 = new FakeMessageChannel();
FakeMessageChannel channel2 = new FakeMessageChannel();
channel1.connect(channel2);
channel1.addListener(TextMessage.class, msg -> {
    ...
});
channel1.start();
channel2.start();
channel2.send("Hello");
```

