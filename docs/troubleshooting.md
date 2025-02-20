# Deepgram Java SDK Troubleshooting Guide

## Common Issues and Solutions

### Connection Issues

#### 1. Connection Timeout
**Symptom:** Connection attempt times out without establishing connection.

**Possible Causes:**
- Network connectivity issues
- Incorrect API key
- Firewall blocking WebSocket connection

**Solutions:**
```java
// 1. Increase connection timeout
DeepgramWebSocket client = new DeepgramWebSocket(
    "wss://api.deepgram.com/v1/listen",
    apiKey,
    new WebSocketConfig().setConnectTimeout(10000)  // 10 seconds
);

// 2. Verify API key is valid
if (apiKey == null || apiKey.trim().isEmpty()) {
    throw new IllegalArgumentException("API key not found");
}

// 3. Implement connection retry logic
int maxRetries = 3;
int retryDelay = 1000;
int attempts = 0;

while (attempts < maxRetries) {
    try {
        client.connect().get(5, TimeUnit.SECONDS);
        break;
    } catch (Exception e) {
        attempts++;
        if (attempts < maxRetries) {
            Thread.sleep(retryDelay);
        }
    }
}
```

#### 2. Authentication Failed
**Symptom:** Connection closed with code 4001 or authentication error.

**Solutions:**
1. Check API key format
2. Verify API key is active in Deepgram Console
3. Ensure API key has appropriate permissions

```java
// Verify API key before connecting
if (!apiKey.matches("^[a-zA-Z0-9]{32,}$")) {
    throw new IllegalArgumentException("Invalid API key format");
}
```

### Audio Streaming Issues

#### 1. Empty Transcripts
**Symptom:** Receiving empty transcript results.

**Possible Causes:**
- Incorrect audio format parameters
- Audio data not being sent properly
- WAV header not being skipped

**Solutions:**
```java
// 1. Verify audio format matches the settings
AudioStreamOptions options = new AudioStreamOptions()
    .setEncoding("linear16")
    .setSampleRate(44100)    // Must match actual audio
    .setChannels(2);         // Must match actual audio

// 2. Check WAV header handling
try (FileInputStream fis = new FileInputStream(audioFile)) {
    // Skip WAV header (44 bytes)
    long skipped = fis.skip(44);
    if (skipped != 44) {
        throw new IOException("Failed to skip WAV header");
    }
    // Continue with streaming...
}

// 3. Verify audio data is being sent
client.setOnTranscript(transcript -> {
    logger.debug("Received transcript, length: {}",
        transcript.getTranscript().length());
});
```

#### 2. Audio Timing Issues
**Symptom:** Transcription results are delayed or garbled.

**Solutions:**
```java
// 1. Calculate and use correct chunk size
int bytesPerSecond = sampleRate * channels * (bitsPerSample / 8);
int chunkSize = bytesPerSecond / 10;  // 100ms chunks

// 2. Implement proper timing
long chunkDurationMs = (long) ((chunkSize / (bytesPerSample * channels)) 
    * 1000.0 / sampleRate);

while ((bytesRead = audioStream.read(buffer)) != -1) {
    client.sendAudio(buffer);
    Thread.sleep(chunkDurationMs);
}
```

### Error Handling

#### 1. WebSocket Errors
**Symptom:** WebSocket connection errors or unexpected disconnects.

**Solutions:**
```java
client.setOnError(error -> {
    logger.error("WebSocket error: {}", error);
    if (error.contains("Connection refused")) {
        // Handle connection issues
    } else if (error.contains("Authentication failed")) {
        // Handle auth issues
    }
});

client.setOnClose(code -> {
    switch (code) {
        case 1000: // Normal closure
            logger.info("Connection closed normally");
            break;
        case 1006: // Abnormal closure
            logger.warn("Connection closed abnormally");
            // Attempt reconnection
            break;
        case 4001: // Authentication failed
            logger.error("Authentication failed");
            break;
        default:
            logger.warn("Connection closed with code: {}", code);
    }
});
```

#### 2. Resource Leaks
**Symptom:** Memory usage grows over time or resources not released.

**Solutions:**
```java
// 1. Use try-with-resources
try (FileInputStream fis = new FileInputStream(audioFile)) {
    // Stream audio...
} finally {
    client.disconnect();
}

// 2. Implement shutdown hook
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    if (client != null) {
        client.disconnect();
    }
}));
```

## Debugging Tips

### 1. Enable Debug Logging
```xml
<!-- In logback.xml -->
<logger name="ai.deepgram.sdk" level="DEBUG"/>
```

### 2. Monitor WebSocket Traffic
```java
client.setOnTranscript(transcript -> {
    logger.debug("Received transcript: {}", transcript.getTranscript());
    logger.debug("Is interim: {}", transcript.isInterim());
});

// Log audio chunks being sent
int totalBytesSent = 0;
while ((bytesRead = audioStream.read(buffer)) != -1) {
    client.sendAudio(buffer);
    totalBytesSent += bytesRead;
    logger.debug("Sent {} bytes, total: {}", bytesRead, totalBytesSent);
}
```

### 3. Performance Monitoring
```java
// Track timing
long startTime = System.currentTimeMillis();
client.setOnTranscript(transcript -> {
    long latency = System.currentTimeMillis() - startTime;
    logger.debug("Transcription latency: {}ms", latency);
});
```

## FAQ

### Q: Why am I getting 400 Bad Request errors?
A: Usually due to mismatched audio format parameters. Ensure your `AudioStreamOptions` match your actual audio format (sample rate, channels, encoding).

### Q: Why are my transcripts delayed?
A: Check your chunk size and streaming rate. Ensure you're sending audio data at real-time speed (use appropriate sleep intervals between chunks).

### Q: How do I handle reconnection?
A: Implement a reconnection strategy with exponential backoff:
```java
private void connectWithRetry(int maxRetries, long initialDelay) {
    long delay = initialDelay;
    int attempts = 0;
    
    while (attempts < maxRetries) {
        try {
            client.connect().get(5, TimeUnit.SECONDS);
            return;
        } catch (Exception e) {
            attempts++;
            if (attempts < maxRetries) {
                Thread.sleep(delay);
                delay *= 2; // Exponential backoff
            }
        }
    }
    throw new RuntimeException("Failed to connect after " + maxRetries + " attempts");
}
```

### Q: How do I optimize performance?
A: Follow these tips:
1. Use appropriate chunk sizes (100ms of audio)
2. Enable interim results only if needed
3. Use mono audio when possible
4. Choose appropriate sample rate for your use case
5. Implement proper error handling and reconnection logic 