# Deepgram Java SDK Usage Examples

## Basic Examples

### 1. Simple Connection Test
Test basic connectivity to Deepgram's WebSocket API:

```java
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.util.EnvConfig;

public class SimpleConnectionTest {
    public static void main(String[] args) {
        String apiKey = EnvConfig.getDeepgramApiKey();
        DeepgramWebSocket client = new DeepgramWebSocket(
            "wss://api.deepgram.com/v1/listen",
            apiKey
        );

        client.setOnOpen(unused -> {
            System.out.println("Connected to Deepgram!");
        });

        client.setOnError(error -> {
            System.err.println("Error: " + error);
        });

        client.connect().thenAccept(unused -> {
            // Connection successful
            client.disconnect();
        });
    }
}
```

### 2. Stream Audio from File
Stream audio from a WAV file in real-time:

```java
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.websocket.AudioStreamOptions;

public class AudioStreamingExample {
    private static final int CHUNK_SIZE = 8820;    // 100ms of stereo audio at 44.1kHz
    private static final int HEADER_SIZE = 44;     // WAV header size

    public static void main(String[] args) {
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(44100)
            .setChannels(2)
            .setModel("nova-2")
            .setLanguage("en-US");

        DeepgramWebSocket client = new DeepgramWebSocket(
            "wss://api.deepgram.com/v1/listen",
            EnvConfig.getDeepgramApiKey()
        );

        client.setOnTranscript(transcript -> {
            System.out.println("Transcript: " + transcript.getTranscript());
        });

        client.connect().thenAccept(unused -> {
            streamAudioFile(client, "audio.wav");
        });
    }

    private static void streamAudioFile(DeepgramWebSocket client, String filename) 
            throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            fis.skip(HEADER_SIZE);  // Skip WAV header
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                if (bytesRead < buffer.length) {
                    byte[] trimmed = new byte[bytesRead];
                    System.arraycopy(buffer, 0, trimmed, 0, bytesRead);
                    client.sendAudio(trimmed);
                } else {
                    client.sendAudio(buffer);
                }
                Thread.sleep(100);  // Simulate real-time streaming
            }
        }
    }
}
```

### 3. Stream from Microphone
Stream audio from the system microphone in real-time:

```java
import javax.sound.sampled.*;

public class MicrophoneStreamingExample {
    public static void main(String[] args) {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        DeepgramWebSocket client = new DeepgramWebSocket(
            "wss://api.deepgram.com/v1/listen",
            EnvConfig.getDeepgramApiKey()
        );

        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setChannels(1);

        client.connect().thenAccept(unused -> {
            try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();

                byte[] buffer = new byte[4096];
                while (true) {
                    int count = line.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        client.sendAudio(buffer);
                    }
                }
            }
        });
    }
}
```

## Advanced Examples

### 1. Error Handling and Reconnection
Implement robust error handling and automatic reconnection:

```java
public class RobustStreamingExample {
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public static void main(String[] args) {
        DeepgramWebSocket client = createClient();
        connectWithRetry(client, MAX_RETRIES);
    }

    private static void connectWithRetry(DeepgramWebSocket client, int retriesLeft) {
        client.connect().thenAccept(unused -> {
            // Connection successful, start streaming
            streamAudio(client);
        }).exceptionally(e -> {
            if (retriesLeft > 0) {
                System.err.println("Connection failed, retrying...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                    connectWithRetry(client, retriesLeft - 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } else {
                System.err.println("Max retries reached");
            }
            return null;
        });
    }
}
```

### 2. Advanced Configuration
Use advanced configuration options:

```java
AudioStreamOptions options = new AudioStreamOptions()
    .setEncoding("linear16")
    .setSampleRate(16000)
    .setChannels(1)
    .setLanguage("en-US")
    .setModel("nova-2")
    .setTier("enhanced")
    .setPunctuate(true)
    .setDiarize(true)
    .setInterimResults(true)
    .setVersion("latest");
```

### 3. Handle Interim Results
Process both interim and final transcription results:

```java
client.setOnTranscript(transcript -> {
    if (transcript.isInterim()) {
        System.out.print("\rInterim: " + transcript.getTranscript());
    } else {
        System.out.println("\nFinal: " + transcript.getTranscript());
        
        // Process word-level information
        for (TranscriptMessage.Word word : transcript.getWords()) {
            System.out.printf("Word: %s (%.2f - %.2f) Confidence: %.2f%n",
                word.getWord(),
                word.getStart(),
                word.getEnd(),
                word.getConfidence()
            );
        }
    }
});
```

### 4. Connection Pool Usage
Manage multiple concurrent connections using the connection pool:

```java
import ai.deepgram.sdk.pool.*;

public class ConnectionPoolExample {
    public static void main(String[] args) {
        // Configure pool settings
        PoolConfig config = new PoolConfig()
            .setInitialSize(3)          // Start with 3 connections
            .setMaxSize(5)              // Allow up to 5 connections
            .setKeepAliveInterval(30000) // Keep-alive every 30s
            .setConnectionTimeout(60000) // Close idle connections after 60s
            .setAcquireTimeout(5000);    // Wait up to 5s for connection

        // Configure audio options
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setChannels(1)
            .setModel("nova-2");

        // Create connection pool
        try (DeepgramConnectionPool pool = new DeepgramConnectionPool(
                "wss://api.deepgram.com/v1/listen",
                EnvConfig.getDeepgramApiKey(),
                config,
                options)) {

            // Acquire and use connections
            for (int i = 0; i < 3; i++) {
                int streamId = i;
                CompletableFuture.runAsync(() -> {
                    try {
                        // Get connection from pool
                        PooledDeepgramConnection conn = pool.acquire();
                        try {
                            DeepgramWebSocket client = conn.getConnection();
                            
                            // Set up handlers
                            client.setOnTranscript(transcript -> {
                                System.out.printf("Stream %d: %s%n", 
                                    streamId, transcript.getTranscript());
                            });

                            // Stream audio
                            streamAudioFile(client, "audio.wav");
                        } finally {
                            // Return connection to pool
                            pool.release(conn);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // Wait for streams to complete
            Thread.sleep(30000);
        }
    }
}
```

Key features of the connection pool:
- Manages a pool of reusable WebSocket connections
- Automatically handles connection lifecycle
- Provides connection health monitoring
- Collects performance metrics
- Implements automatic cleanup of idle connections
- Supports concurrent streaming with multiple connections

Monitor pool metrics:
```java
PoolMetrics metrics = pool.getMetrics();
System.out.printf("Active connections: %d%n", metrics.getActiveConnections());
System.out.printf("Idle connections: %d%n", metrics.getIdleConnections());
System.out.printf("Average acquisition time: %d ms%n", 
    metrics.getAverageAcquisitionTime());
```

## Best Practices

1. Always clean up resources:
```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    if (client != null) {
        client.disconnect();
    }
}));
```

2. Use appropriate chunk sizes for streaming:
```java
// Calculate chunk size based on audio format
int bytesPerSecond = sampleRate * channels * (bitsPerSample / 8);
int chunkSize = bytesPerSecond / 10; // 100ms chunks
```

3. Handle connection state properly:
```java
CountDownLatch connectionLatch = new CountDownLatch(1);
client.setOnOpen(unused -> connectionLatch.countDown());
if (!connectionLatch.await(5, TimeUnit.SECONDS)) {
    // Handle connection timeout
}
``` 