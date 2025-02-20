package ai.deepgram.sdk.examples;

import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.util.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleConnectionTest.class);

    public static void main(String[] args) {
        try {
            String apiKey = EnvConfig.getDeepgramApiKey();
            CountDownLatch connectionLatch = new CountDownLatch(1);

            // Create audio stream options
            AudioStreamOptions options = new AudioStreamOptions()
                .setEncoding("linear16")
                .setSampleRate(16000)
                .setChannels(1)
                .setModel("nova-2")
                .setInterimResults(true);

            DeepgramWebSocket client = new DeepgramWebSocket(
                "wss://api.deepgram.com/v1/listen",
                apiKey
            );

            client.setOnOpen(unused -> {
                logger.info("Connected to Deepgram WebSocket API!");
                connectionLatch.countDown();
            });

            client.setOnError(error -> {
                logger.error("WebSocket error: {}", error);
                connectionLatch.countDown();
            });

            client.setOnClose(code -> {
                logger.info("Connection closed with code: {}", code);
            });

            logger.info("Connecting to Deepgram WebSocket API...");
            client.setOptions(options);
            client.connect().thenAccept(unused -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    client.disconnect();
                }
            }).exceptionally(e -> {
                logger.error("Connection error", e);
                return null;
            });

            try {
                if (!connectionLatch.await(5, TimeUnit.SECONDS)) {
                    logger.error("Connection timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while waiting for connection", e);
            }

            logger.info("Test completed");
        } catch (IllegalStateException e) {
            logger.error("Failed to get API key: {}", e.getMessage());
        }
    }
} 