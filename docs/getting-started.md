# Getting Started with Deepgram Java SDK

## Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- A Deepgram API key (get one from [Deepgram Console](https://console.deepgram.com))

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ai.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## API Key Configuration

There are two ways to configure your Deepgram API key:

### 1. Environment Variable
Set the `DEEPGRAM_API_KEY` environment variable:

```bash
export DEEPGRAM_API_KEY=your_api_key_here
```

### 2. .env File
Create a `.env` file in your project root:

```properties
DEEPGRAM_API_KEY=your_api_key_here
```

The SDK will automatically load the API key from either source.

## Basic Setup

1. Create a new WebSocket client:

```java
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.util.EnvConfig;

String apiKey = EnvConfig.getDeepgramApiKey();
DeepgramWebSocket client = new DeepgramWebSocket(
    "wss://api.deepgram.com/v1/listen",
    apiKey
);
```

2. Configure audio stream options:

```java
import ai.deepgram.sdk.websocket.AudioStreamOptions;

AudioStreamOptions options = new AudioStreamOptions()
    .setEncoding("linear16")
    .setSampleRate(16000)
    .setChannels(1)
    .setLanguage("en-US")
    .setModel("nova-2");
```

3. Set up event handlers:

```java
client.setOnOpen(unused -> {
    System.out.println("Connected to Deepgram!");
});

client.setOnTranscript(transcript -> {
    System.out.println("Transcript: " + transcript.getTranscript());
    System.out.println("Confidence: " + transcript.getConfidence());
});

client.setOnError(error -> {
    System.err.println("Error: " + error);
});

client.setOnClose(code -> {
    System.out.println("Connection closed with code: " + code);
});
```

4. Connect and handle the connection:

```java
client.connect().thenAccept(unused -> {
    // Start streaming audio after connection is established
    streamAudio(client);
}).exceptionally(e -> {
    System.err.println("Connection error: " + e.getMessage());
    return null;
});
```

## Audio Format Support

The SDK supports various audio formats:

| Format | Encoding | Sample Rates | Channels |
|--------|----------|--------------|----------|
| WAV    | linear16 | 8000-48000   | 1-2      |
| Raw PCM| linear16 | 8000-48000   | 1-2      |
| Opus   | opus     | 8000-48000   | 1-2      |
| µ-law  | mulaw    | 8000         | 1        |

## Running Tests

The SDK includes several tests that demonstrate different features and usage patterns. You can run these tests using Maven profiles for easy execution.

### Available Tests

1. **Audio Streaming Test** (`-P stream`)
   - Demonstrates real-time audio streaming
   - Processes a sample WAV file
   - Shows latency measurements and transcription results
   ```bash
   mvn compile exec:java -P stream
   ```

2. **Simple Connection Test** (`-P simple`)
   - Basic connectivity test
   - Demonstrates connection setup and error handling
   - Useful for verifying API key and network connectivity
   ```bash
   mvn compile exec:java -P simple
   ```

3. **Connection Pool Test** (`-P pool`)
   - Advanced usage with connection pooling
   - Processes multiple streams concurrently
   - Shows pool metrics and performance statistics
   ```bash
   mvn compile exec:java -P pool
   ```

### Test Output

Each test provides detailed logging output including:

- **Connection Metrics**
  - Time to establish WebSocket connection
  - First Results message latency (initial server response)
  - First transcript latency (first actual transcription)

- **Transcription Results**
  - Speech-final transcripts (completed utterances)
  - Interim results (if enabled)
  - Confidence scores (optional)

- **Error Handling**
  - Connection issues
  - Audio streaming problems
  - API errors

### Running Unit Tests

To run the standard unit tests:
```bash
mvn test
```

### Troubleshooting Tests

1. **API Key Issues**
   - Ensure your API key is set in `.env` file or environment
   - Check the key has necessary permissions

2. **Audio File Issues**
   - Verify `bueller.wav` exists in project root
   - Check audio format (44.1kHz, 16-bit, stereo)

3. **Connection Issues**
   - Check network connectivity
   - Verify firewall settings
   - Look for SSL/TLS problems

## Next Steps
- Check out the [Usage Examples](examples.md) for more detailed examples
- Read the [Configuration Guide](configuration.md) for advanced settings
- See the [Troubleshooting Guide](troubleshooting.md) if you encounter issues 