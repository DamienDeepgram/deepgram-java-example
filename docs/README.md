# Deepgram Java SDK Documentation

## Overview
The Deepgram Java SDK provides a simple and efficient way to integrate real-time audio transcription into your Java applications using Deepgram's WebSocket API. This SDK supports streaming audio in real-time and receiving transcription results with high accuracy.

## Table of Contents
1. [Getting Started](getting-started.md)
   - Installation
   - Basic Setup
   - API Key Configuration

2. [Core Components](core-components.md)
   - DeepgramWebSocket
   - AudioStreamOptions
   - Message Types
   - Event Handling

3. [Usage Examples](examples.md)
   - Simple Connection
   - Real-time Audio Streaming
   - Error Handling
   - Advanced Configuration

4. [Configuration Guide](configuration.md)
   - Audio Format Settings
   - Transcription Options
   - Connection Parameters
   - Environment Setup

5. [Troubleshooting](troubleshooting.md)
   - Common Issues
   - Error Messages
   - Best Practices
   - FAQ

## Quick Start

```java
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.util.EnvConfig;

// Get API key from environment or .env file
String apiKey = EnvConfig.getDeepgramApiKey();

// Create WebSocket client
DeepgramWebSocket client = new DeepgramWebSocket(
    "wss://api.deepgram.com/v1/listen",
    apiKey
);

// Configure audio stream options
AudioStreamOptions options = new AudioStreamOptions()
    .setEncoding("linear16")
    .setSampleRate(16000)
    .setChannels(1)
    .setLanguage("en-US")
    .setModel("nova-2")
    .setInterimResults(true);

// Set up event handlers
client.setOnOpen(unused -> {
    System.out.println("Connected to Deepgram!");
});

client.setOnTranscript(transcript -> {
    System.out.println("Transcript: " + transcript.getTranscript());
});

client.setOnError(error -> {
    System.err.println("Error: " + error);
});

// Connect and start streaming
client.connect().thenAccept(unused -> {
    // Start streaming audio...
});
```

## License
This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details. 