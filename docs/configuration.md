# Deepgram Java SDK Configuration Guide

## Audio Stream Options

The `AudioStreamOptions` class provides a comprehensive set of configuration options for the audio stream. Here's a detailed guide to all available options:

### Basic Audio Settings

```java
AudioStreamOptions options = new AudioStreamOptions()
    .setEncoding("linear16")      // Audio encoding format
    .setSampleRate(16000)         // Sample rate in Hz
    .setChannels(1)               // Number of audio channels
    .setLanguage("en-US");        // Language code
```

### Supported Audio Formats

| Setting | Values | Description |
|---------|--------|-------------|
| encoding | "linear16", "opus", "mulaw" | Audio encoding format |
| sampleRate | 8000-48000 | Audio sample rate in Hz |
| channels | 1-2 | Number of audio channels |

### Transcription Settings

```java
options.setModel("nova-2")           // Transcription model
       .setPunctuate(true)           // Add punctuation
       .setDiarize(true)             // Enable speaker diarization
       .setInterimResults(true)      // Enable interim results
       .setTier("enhanced");         // API tier
```

### Model Options

| Model | Description | Use Case |
|-------|-------------|----------|
| nova-2 | Latest general-purpose model | Best for most use cases |
| base | Balanced accuracy/speed | Good for real-time |
| enhanced | Highest accuracy | When accuracy is critical |

### Language Support

```java
// Single language
options.setLanguage("en-US");

// Multiple languages (auto-detect)
options.setLanguage("en,es,fr");
```

Supported language codes:
- English: "en-US", "en-GB", "en-AU", "en-NZ", "en-IN"
- Spanish: "es", "es-419"
- French: "fr"
- German: "de"
- And many more...

### Advanced Features

#### Diarization (Speaker Detection)
```java
options.setDiarize(true)             // Enable speaker detection
       .setDiarizeVersion("latest"); // Use latest diarization model
```

#### Interim Results
```java
options.setInterimResults(true)      // Enable interim results
       .setWordTimings(true);        // Include word-level timing
```

#### Smart Formatting
```java
options.setPunctuate(true)           // Add punctuation
       .setNumerals(true)            // Convert numbers to text
       .setSpelling("us");           // Use US spelling
```

## WebSocket Configuration

### Connection Settings

```java
DeepgramWebSocket client = new DeepgramWebSocket(
    "wss://api.deepgram.com/v1/listen",  // Base URL
    apiKey,                              // API key
    new WebSocketConfig()                // Optional config
        .setConnectTimeout(5000)         // Connection timeout (ms)
        .setReadTimeout(30000)           // Read timeout (ms)
        .setWriteTimeout(30000)          // Write timeout (ms)
        .setMaxRetries(3)                // Max reconnection attempts
        .setRetryDelay(1000)            // Delay between retries (ms)
);
```

### SSL/TLS Settings

```java
WebSocketConfig config = new WebSocketConfig()
    .setTrustAllCerts(false)            // Don't trust all certificates
    .setVerifyHostname(true)            // Verify hostname
    .setKeyStore("path/to/keystore")    // Custom keystore
    .setKeyStorePassword("password");   // Keystore password
```

## Environment Configuration

### API Key Configuration

1. Environment Variable:
```bash
export DEEPGRAM_API_KEY=your_api_key_here
```

2. .env File:
```properties
DEEPGRAM_API_KEY=your_api_key_here
```

3. Programmatic:
```java
System.setProperty("DEEPGRAM_API_KEY", "your_api_key_here");
```

### Logging Configuration

1. Using logback.xml:
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="ai.deepgram.sdk" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

2. Programmatic:
```java
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
root.setLevel(Level.INFO);

Logger sdkLogger = (Logger) LoggerFactory.getLogger("ai.deepgram.sdk");
sdkLogger.setLevel(Level.DEBUG);
```

## Best Practices

1. **Audio Format Selection**
   - Use 16kHz sample rate for speech recognition
   - Use mono audio when possible
   - Use linear16 encoding for best compatibility

2. **Performance Optimization**
   - Use appropriate chunk sizes (100ms recommended)
   - Enable interim results only when needed
   - Use connection pooling for multiple streams

3. **Error Handling**
   - Implement reconnection logic
   - Handle timeouts appropriately
   - Log errors for debugging

4. **Resource Management**
   - Always close connections properly
   - Use try-with-resources for streams
   - Implement shutdown hooks 