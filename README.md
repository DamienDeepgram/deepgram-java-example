# Deepgram Java SDK

A Java SDK for real-time audio transcription using Deepgram's WebSocket API.

## Features

- Real-time audio streaming over WebSocket
- Type-safe message handling
- Configurable transcription options
- Automatic reconnection handling
- Comprehensive error handling
- Event-based architecture

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ai.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

1. Get your API key from [Deepgram Console](https://console.deepgram.com)

2. Set up your API key by either:
   - Creating a `.env` file based on `.env.example` and setting your API key there
   - Setting the `DEEPGRAM_API_KEY` environment variable

3. Use the SDK in your code:

```java
// The API key will be automatically loaded from environment variables or .env file
DeepgramWebSocket client = new DeepgramWebSocket();
AudioStreamOptions options = new AudioStreamOptions()
    .setEncoding("linear16")
    .setSampleRate(16000)
    .setLanguage("en-US")
    .setModel("nova-2")
    .setInterimResults(true);

client.onOpen(() -> {
    // Start streaming audio
});

client.onTranscript(result -> {
    System.out.println("Transcript: " + result.getTranscript());
});

client.onError(error -> {
    System.err.println("Error: " + error.getMessage());
});

client.connect(options);
```

## Running Tests

### Unit Tests
Run all unit tests with:
```bash
mvn test
```

### Example Tests
We have several example tests that demonstrate different features:

1. Audio Streaming Test (single connection):
```bash
mvn compile exec:java -P stream
```

2. Simple Connection Test (basic connectivity):
```bash
mvn compile exec:java -P simple
```

3. Connection Pool Test (multiple streams):
```bash
mvn compile exec:java -P pool
```

Each test will output detailed logs including:
- Connection time
- First Results message latency
- First transcript latency
- Full transcription results

## Building from Source

1. Clone the repository
2. Run `mvn clean install`

## Documentation

Detailed documentation is available in the `docs` directory.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 