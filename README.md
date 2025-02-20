# Deepgram Java SDK

A Java SDK for real-time audio transcription using Deepgram's WebSocket API.

## Features

- Real-time audio streaming over WebSocket
- Type-safe message handling
- Configurable transcription options
- Automatic reconnection handling
- Comprehensive error handling
- Event-based architecture
- Connection pooling for multiple streams

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

## Development Setup

1. Clone the repository:
```bash
git clone https://github.com/DamienDeepgram/deepgram-java-example
cd deepgram-java-example
```

2. Copy the environment file:
```bash
cp .env.example .env
```

3. Add your Deepgram API key to `.env`

4. Build the project:
```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Or using system Maven
mvn clean install
```

## Running Tests

### Integration Tests
We use integration tests with the real Deepgram API to ensure the SDK works correctly in production scenarios. These tests require a valid Deepgram API key:

1. Audio Streaming Test (single connection):
```bash
mvn compile exec:java -P stream
```

2. Connection Pool Test (multiple concurrent streams):
```bash
mvn compile exec:java -P pool
```

The tests will output detailed metrics including:
- Connection establishment time
- First message latency
- First transcript latency
- Transcription accuracy
- Connection pool utilization

For detailed information about our testing approach and results, see [Testing Guide](docs/testing.md).

### Building Without Tests
If you want to build the project without running tests:
```bash
mvn clean install -DskipTests
```

### Test Requirements
- Valid Deepgram API key in `.env` file
- Java 11 or higher
- Maven 3.6 or higher
- Internet connection for API access

## Building from Source

1. Clone the repository
2. Run `mvn clean install`

## Troubleshooting

### Common Issues

1. Connection Errors
   - Verify your API key is correct
   - Check your internet connection
   - Ensure you're not behind a restrictive firewall

2. Audio Streaming Issues
   - Verify your audio format matches the configuration
   - Check that sample rate and encoding are set correctly
   - Ensure audio file is accessible and not corrupted

3. Build Issues
   - Ensure Java 11+ is installed: `java -version`
   - Verify Maven installation: `mvn -version`
   - Try using the Maven wrapper: `./mvnw`

### Debug Logging

To enable debug logging, add the following to your `logback.xml`:

```xml
<logger name="ai.deepgram" level="DEBUG"/>
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Documentation

Detailed documentation is available in the `docs` directory.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 