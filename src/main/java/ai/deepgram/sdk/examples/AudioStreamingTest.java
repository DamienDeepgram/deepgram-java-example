package ai.deepgram.sdk.examples;

import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.util.EnvConfig;
import ai.deepgram.sdk.websocket.TranscriptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AudioStreamingTest {
    private static final Logger logger = LoggerFactory.getLogger(AudioStreamingTest.class);
    private static final String AUDIO_FILE = "src/test/resources/bueller.wav";
    private static final int SAMPLE_RATE = 44100;  // Updated to match WAV file
    private static final int CHANNELS = 2;         // Updated to match WAV file
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit audio
    private static final int HEADER_SIZE = 44;     // WAV header size
    private static final int CHUNK_SIZE = 8820;    // 100ms of stereo audio at 44.1kHz (44100 * 2 * 2 * 0.1)
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            String apiKey = EnvConfig.getDeepgramApiKey();
            CountDownLatch transcriptionLatch = new CountDownLatch(1);
            AtomicLong connectStartTime = new AtomicLong(0);
            AtomicLong streamStartTime = new AtomicLong(0);
            AtomicLong firstResultTime = new AtomicLong(0);
            AtomicLong firstTranscriptTime = new AtomicLong(0);

            // Create audio stream options
            AudioStreamOptions options = new AudioStreamOptions()
                .setEncoding("linear16")
                .setSampleRate(SAMPLE_RATE)
                .setChannels(CHANNELS)
                .setModel("nova-2")
                .setInterimResults(true);

            DeepgramWebSocket client = new DeepgramWebSocket(
                "wss://api.deepgram.com/v1/listen",
                apiKey
            );

            client.setOnOpen(unused -> {
                long connectionTime = System.currentTimeMillis() - connectStartTime.get();
                logger.info("Connected to Deepgram WebSocket API! (took {} ms)", connectionTime);
                streamStartTime.set(System.currentTimeMillis());
            });

            client.setOnError(error -> {
                logger.error("WebSocket error: {}", error);
            });

            client.setOnClose(code -> {
                logger.info("Connection closed with code: {}", code);
                transcriptionLatch.countDown();
            });

            client.setOnRawMessage(message -> {
                try {
                    TranscriptResponse response = objectMapper.readValue(message, TranscriptResponse.class);
                    if (firstResultTime.get() == 0 && "Results".equals(response.getType())) {
                        firstResultTime.set(System.currentTimeMillis() - streamStartTime.get());
                        logger.info("First Results message received after {} ms", firstResultTime.get());
                    }

                    // Handle transcript logging
                    if (response.getChannel() != null && 
                        response.getChannel().getAlternatives() != null && 
                        !response.getChannel().getAlternatives().isEmpty()) {
                        
                        String text = response.getChannel().getAlternatives().get(0).getTranscript();

                        if (text != null && !text.trim().isEmpty()) {
                            if (firstTranscriptTime.get() == 0) {
                                firstTranscriptTime.set(System.currentTimeMillis() - streamStartTime.get());
                                logger.info("First transcript with content received after {} ms", firstTranscriptTime.get());
                            }

                            if (response.isSpeechFinal() && response.isFinal()) {
                                logger.info("  [Speech Final] '{}'", text);
                            } else if (response.isFinal()) {
                                logger.info("  [Is Final] '{}'", text);
                            } else {
                                logger.info("    [Interim Result] '{}'", text);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error parsing message: {} - {}", message, e.getMessage());
                }
            });

            client.setOnTranscript(transcript -> {
                // No need to track first transcript time here since we're doing it in onRawMessage
            });

            logger.info("Connecting to Deepgram WebSocket API...");
            connectStartTime.set(System.currentTimeMillis());
            client.setOptions(options);
            client.connect().thenAccept(unused -> {
                logger.info("Connection established, starting audio stream...");
                CompletableFuture.runAsync(() -> {
                    try {
                        streamAudio(client);
                    } catch (Exception e) {
                        logger.error("Error streaming audio", e);
                    } finally {
                        client.disconnect();
                    }
                });
            }).exceptionally(e -> {
                logger.error("Connection error", e);
                return null;
            });

            try {
                if (!transcriptionLatch.await(30, TimeUnit.SECONDS)) {
                    logger.error("Transcription timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while waiting for transcription", e);
            }

            // Log final metrics
            logger.info("\nStreaming completed. Metrics:");
            long connectionTime = streamStartTime.get() - connectStartTime.get();
            logger.info("  Connection time: {} ms", connectionTime);
            logger.info("  First Results message latency: {} ms", firstResultTime.get());
            logger.info("  First transcript latency: {} ms", firstTranscriptTime.get());
            logger.info("Test completed");
        } catch (IllegalStateException e) {
            logger.error("Failed to get API key: {}", e.getMessage());
        }
    }

    private static void streamAudio(DeepgramWebSocket client) throws IOException {
        File audioFile = new File(AUDIO_FILE);
        if (!audioFile.exists()) {
            throw new IOException("Audio file not found: " + AUDIO_FILE);
        }

        logger.info("Streaming audio from file: {}", audioFile.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            // Skip WAV header
            fis.skip(HEADER_SIZE);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            long chunkDurationMs = (long) ((CHUNK_SIZE / (BYTES_PER_SAMPLE * CHANNELS)) * 1000.0 / SAMPLE_RATE);

            while ((bytesRead = fis.read(buffer)) != -1) {
                if (bytesRead < buffer.length) {
                    // Trim the buffer for the last chunk
                    byte[] trimmedBuffer = new byte[bytesRead];
                    System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
                    client.sendAudio(trimmedBuffer);
                } else {
                    client.sendAudio(buffer);
                }

                // Sleep for the duration of the chunk to simulate real-time streaming
                try {
                    Thread.sleep(chunkDurationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
} 