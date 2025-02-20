package ai.deepgram.sdk.pool;

import ai.deepgram.sdk.message.TranscriptMessage;
import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.util.EnvConfig;
import ai.deepgram.sdk.websocket.TranscriptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolStreamingTest {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolStreamingTest.class);
    private static final String AUDIO_FILE = "src/test/resources/bueller.wav";
    private static final int CHUNK_SIZE = 8820; // ~50ms of audio at 44.1kHz stereo
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final boolean DEBUG = false;  // Set to true to enable debug logging
    private static final int NUM_CONNECTIONS = 3;
    private static final int START_DELAY_MS = 1000; // 1 second delay between starts
    private static final int TOTAL_STREAMS_TO_PROCESS = 6; // Total number of streams to process
    private static final int CLOSE_WAIT_MS = 500; // Wait time before starting new stream after close

    private File audioFile;
    private DeepgramConnectionPool connectionPool;
    private AudioStreamOptions streamOptions;
    private String apiKey;
    private CountDownLatch allStreamsCompleteLatch;
    private AtomicInteger streamsStarted;
    private AtomicInteger streamsCompleted;
    private ConcurrentHashMap<String, Long> firstMessageTimes;
    private ConcurrentHashMap<String, Long> firstTranscriptTimes;
    private AtomicBoolean isShuttingDown;

    @BeforeEach
    public void setUp() {
        apiKey = EnvConfig.getDeepgramApiKey();
        audioFile = new File(AUDIO_FILE);
        if (!audioFile.exists()) {
            throw new IllegalStateException("Test audio file not found: " + AUDIO_FILE);
        }

        streamOptions = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(44100)
            .setChannels(2)
            .setModel("nova-2")
            .setInterimResults(true);

        PoolConfig poolConfig = new PoolConfig()
            .setInitialSize(NUM_CONNECTIONS)
            .setMaxSize(NUM_CONNECTIONS)
            .setKeepAliveInterval(10000)
            .setConnectionTimeout(60000)
            .setAcquireTimeout(5000);

        connectionPool = new DeepgramConnectionPool(
            "wss://api.deepgram.com/v1/listen",
            apiKey,
            poolConfig,
            streamOptions
        );

        allStreamsCompleteLatch = new CountDownLatch(TOTAL_STREAMS_TO_PROCESS);
        streamsStarted = new AtomicInteger(0);
        streamsCompleted = new AtomicInteger(0);
        firstMessageTimes = new ConcurrentHashMap<>();
        firstTranscriptTimes = new ConcurrentHashMap<>();
        isShuttingDown = new AtomicBoolean(false);
    }

    @Test
    void streamSingleAudioFile() throws Exception {
        try {
            // Start initial batch of streams
            for (int i = 0; i < NUM_CONNECTIONS; i++) {
                startNewStream();
                Thread.sleep(START_DELAY_MS);
            }

            // Wait for all streams to complete
            if (!allStreamsCompleteLatch.await(120, TimeUnit.SECONDS)) {
                throw new RuntimeException("Test timed out waiting for streams to complete");
            }

            // Log metrics
            logger.info("\nStreaming completed. Metrics:");
            logger.info("  Total streams processed: {}", streamsCompleted.get());
            logger.info("  Pool metrics: {}", connectionPool.getMetrics().toString());
            
            firstMessageTimes.forEach((connId, time) -> 
                logger.info("  [Conn {}] First message received after {} ms", connId, time));
            
            firstTranscriptTimes.forEach((connId, time) -> 
                logger.info("  [Conn {}] First transcript with content received after {} ms", connId, time));
        } finally {
            isShuttingDown.set(true);
            connectionPool.close();
        }
    }

    private void startNewStream() throws Exception {
        int streamId = streamsStarted.incrementAndGet();
        if (streamId > TOTAL_STREAMS_TO_PROCESS || isShuttingDown.get()) {
            return;
        }

        try {
            PooledDeepgramConnection pooledConn = connectionPool.acquire();
            String connId = String.valueOf(streamId);
            DeepgramWebSocket client = pooledConn.getConnection();
            AtomicLong streamStartTime = new AtomicLong(System.currentTimeMillis());

            // Set up message handlers
            client.setOnRawMessage(message -> {
                try {
                    TranscriptResponse response = objectMapper.readValue(message, TranscriptResponse.class);
                    if (!firstMessageTimes.containsKey(connId) && "Results".equals(response.getType())) {
                        firstMessageTimes.put(connId, System.currentTimeMillis() - streamStartTime.get());
                        logger.info("[Conn {}] First message received after {} ms", 
                            connId, firstMessageTimes.get(connId));
                    }

                    // Handle transcript logging here where we have access to all flags
                    if (response.getChannel() != null && 
                        response.getChannel().getAlternatives() != null && 
                        !response.getChannel().getAlternatives().isEmpty()) {
                        
                        String text = response.getChannel().getAlternatives().get(0).getTranscript();
                        double confidence = response.getChannel().getAlternatives().get(0).getConfidence();

                        if (text != null && !text.trim().isEmpty()) {
                            if (response.isSpeechFinal() && response.isFinal()) {
                                logger.info("[Conn {}][Speech Final] '{}' (confidence: {})",
                                    connId, text, confidence);
                            } else if (response.isFinal()) {
                                logger.info("[Conn {}]  [Is Final] '{}' (confidence: {})",
                                    connId, text, confidence);
                            } else {
                                logger.info("[Conn {}]    [Interim Result] '{}' (confidence: {})",
                                    connId, text, confidence);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error parsing message: {} - {}", message, e.getMessage());
                }
            });

            client.setOnTranscript(transcript -> {
                String text = transcript.getTranscript();
                if (text != null && !text.trim().isEmpty() && !firstTranscriptTimes.containsKey(connId)) {
                    firstTranscriptTimes.put(connId, System.currentTimeMillis() - streamStartTime.get());
                    logger.info("[Conn {}] First transcript with content received after {} ms", 
                        connId, firstTranscriptTimes.get(connId));
                }
            });

            client.setOnClose(code -> {
                try {
                    // Instead of releasing, close and remove the connection
                    pooledConn.close();
                    
                    int completed = streamsCompleted.incrementAndGet();
                    allStreamsCompleteLatch.countDown();
                    
                    logger.info("[Conn {}] Connection terminated and removed from pool", connId);
                    
                    // Start a new stream if we haven't reached the total and not shutting down
                    if (completed < TOTAL_STREAMS_TO_PROCESS && !isShuttingDown.get()) {
                        // Add a small delay before starting new stream to allow cleanup
                        Thread.sleep(CLOSE_WAIT_MS);
                        startNewStream();
                    }
                } catch (InterruptedException e) {
                    if (!isShuttingDown.get()) {
                        logger.error("[Conn {}] Error starting new stream: interrupted", connId);
                        allStreamsCompleteLatch.countDown();
                    }
                } catch (Exception e) {
                    logger.error("[Conn {}] Error handling connection close: {}", connId, e.getMessage());
                }
            });

            // Start streaming audio
            new Thread(() -> {
                try {
                    streamAudioFile(client, audioFile);
                } catch (Exception e) {
                    if (!isShuttingDown.get()) {
                        logger.error("Error streaming audio for connection {}: {}", connId, e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            if (!isShuttingDown.get()) {
                logger.error("Error starting new stream: {}", e.getMessage());
                allStreamsCompleteLatch.countDown(); // Count down even on failure
            }
        }
    }

    private void streamAudioFile(DeepgramWebSocket client, File audioFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            // Skip WAV header
            fis.skip(44);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            long chunkDurationMs = (long) ((CHUNK_SIZE / (2 * 2)) * 1000.0 / 44100);

            while ((bytesRead = fis.read(buffer)) != -1 && !isShuttingDown.get()) {
                if (bytesRead < buffer.length) {
                    byte[] trimmedBuffer = new byte[bytesRead];
                    System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
                    client.sendAudio(trimmedBuffer);
                } else {
                    client.sendAudio(buffer);
                }
                Thread.sleep(chunkDurationMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Audio streaming interrupted", e);
        } finally {
            client.disconnect();
        }
    }
} 