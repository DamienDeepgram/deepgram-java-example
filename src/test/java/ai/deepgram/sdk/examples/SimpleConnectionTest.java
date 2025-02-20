package ai.deepgram.sdk.examples;

import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Simple example showing how to connect to Deepgram using the API key from .env
 */
public class SimpleConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleConnectionTest.class);
    private static final String API_KEY = System.getenv("DEEPGRAM_API_KEY");
    private static final String BASE_URL = "wss://api.deepgram.com/v1/listen";

    public static void main(String[] args) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalArgumentException("DEEPGRAM_API_KEY environment variable must be set");
        }

        DeepgramWebSocket client = new DeepgramWebSocket(BASE_URL, API_KEY);

        // Set up event handlers
        client.setOnTranscript(message -> {
            logger.info("Received transcript: {}", message.getTranscript());
        });

        client.setOnError(error -> {
            logger.error("WebSocket error: {}", error);
        });

        client.setOnClose(code -> {
            logger.info("WebSocket closed with code: {}", code);
        });

        // Configure audio stream options
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setChannels(1)
            .setModel("nova-2");

        // Set options and connect
        client.setOptions(options);
        CompletableFuture<Void> future = client.connect();

        // Wait for connection
        future.get();
        logger.info("Connected to Deepgram");

        // Send audio data
        try {
            File audioFile = new File("src/test/resources/test.raw");
            FileInputStream fis = new FileInputStream(audioFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] audioChunk = bytesRead < buffer.length ? 
                    java.util.Arrays.copyOf(buffer, bytesRead) : buffer;
                client.sendAudio(audioChunk);
                Thread.sleep(20); // Simulate real-time streaming
            }

            fis.close();
        } catch (IOException | InterruptedException e) {
            logger.error("Error sending audio: {}", e.getMessage());
        }

        // Wait a bit for final transcripts
        Thread.sleep(1000);

        // Clean up
        client.disconnect();
        logger.info("Test completed");
    }
} 