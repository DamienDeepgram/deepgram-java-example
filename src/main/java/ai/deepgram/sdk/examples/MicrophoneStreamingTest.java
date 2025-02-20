package ai.deepgram.sdk.examples;

import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.util.EnvConfig;
import ai.deepgram.sdk.message.ControlMessage;
import ai.deepgram.sdk.websocket.TranscriptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MicrophoneStreamingTest {
    private static final Logger logger = LoggerFactory.getLogger(MicrophoneStreamingTest.class);
    private static final int SAMPLE_RATE = 16000;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;
    private static final int CHUNK_SIZE = 3200;  // 100ms of audio at 16kHz mono (increased from 2048)
    private static final int BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8;
    private static final int CHUNK_DURATION_MS = (CHUNK_SIZE * 1000) / (SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE);
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);
    private static final long KEEP_ALIVE_INTERVAL = 5000; // 5 seconds
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            String apiKey = EnvConfig.getDeepgramApiKey();
            CountDownLatch exitLatch = new CountDownLatch(1);
            AtomicLong lastTranscriptTime = new AtomicLong(0);

            // List available audio input devices
            List<Mixer.Info> mixerInfos = new ArrayList<>();
            for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                Mixer mixer = AudioSystem.getMixer(info);
                Line.Info[] targetLines = mixer.getTargetLineInfo();
                if (targetLines.length > 0 && targetLines[0].getLineClass().equals(TargetDataLine.class)) {
                    mixerInfos.add(info);
                    logger.info("Found input device: {}", info.getName());
                }
            }

            if (mixerInfos.isEmpty()) {
                throw new RuntimeException("No audio input devices found");
            }

            // Let user select input device
            logger.info("\nAvailable audio input devices:");
            for (int i = 0; i < mixerInfos.size(); i++) {
                logger.info("  {}. {}", i + 1, mixerInfos.get(i).getName());
            }
            logger.info("\nSelect device number (1-{}): ", mixerInfos.size());

            int selection;
            try (Scanner scanner = new Scanner(System.in)) {
                selection = scanner.nextInt() - 1;
            }
            if (selection < 0 || selection >= mixerInfos.size()) {
                throw new RuntimeException("Invalid device selection");
            }

            // Set up audio format for microphone
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,          // Sample rate
                BITS_PER_SAMPLE,      // Sample size in bits
                CHANNELS,             // Channels
                true,                 // Signed
                false                 // Little endian (Deepgram expects little-endian)
            );
            Mixer.Info selectedMixer = mixerInfos.get(selection);
            Mixer mixer = AudioSystem.getMixer(selectedMixer);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!mixer.isLineSupported(info)) {
                logger.error("Selected device does not support format: {} Hz, {}-bit, {} channel(s)", 
                    SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS);
                throw new RuntimeException("Selected device does not support required audio format");
            }

            logger.info("\nAudio device configuration:");
            logger.info("  Device: {}", selectedMixer.getName());
            logger.info("  Format: {} Hz, {}-bit {}, {} channel(s)", 
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getEncoding().toString(),
                format.getChannels());
            logger.info("  Frame size: {} bytes", format.getFrameSize());
            logger.info("  Frame rate: {} fps", format.getFrameRate());

            // Create Deepgram client
            DeepgramWebSocket client = new DeepgramWebSocket(
                "wss://api.deepgram.com/v1/listen",
                apiKey
            );

            // Configure audio stream options
            AudioStreamOptions options = new AudioStreamOptions()
                .setEncoding("linear16")    // 16-bit linear PCM
                .setSampleRate(SAMPLE_RATE) // 16kHz
                .setChannels(CHANNELS)      // Mono
                .setModel("nova-2")
                .setInterimResults(true)
                .setLanguage("en-US");

            logger.info("\nDeepgram configuration:");
            logger.info("  Model: nova-2");
            logger.info("  Language: en-US");
            logger.info("  Interim results: enabled");

            logger.info("\nConnecting to Deepgram...");

            // Set up event handlers
            client.setOnOpen(unused -> {
                logger.info("Connected to Deepgram");
                logger.info("Streaming from microphone. Press Ctrl+C to stop.");

                // Start keep-alive scheduler
                scheduler.scheduleAtFixedRate(() -> {
                    try {
                        if (isRunning.get()) {
                            client.sendKeepAlive();
                        }
                    } catch (Exception e) {
                        logger.error("Error sending keep-alive: {}", e.getMessage());
                    }
                }, KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, TimeUnit.MILLISECONDS);
            });

            client.setOnRawMessage(message -> {
                try {
                    TranscriptResponse response = objectMapper.readValue(message, TranscriptResponse.class);
                    if (response.getChannel() != null && 
                        response.getChannel().getAlternatives() != null && 
                        !response.getChannel().getAlternatives().isEmpty()) {
                        
                        String text = response.getChannel().getAlternatives().get(0).getTranscript();
                        if (text != null && !text.trim().isEmpty()) {
                            logger.debug("Response flags: is_final={}, speech_final={}, from_finalize={}",
                                response.isFinal(), response.isSpeechFinal(), response.isFromFinalize());
                            
                            if (response.isSpeechFinal() && response.isFinal()) {
                                logger.info("  [Speech Final] {}", text);
                            } else if (response.isFinal()) {
                                logger.info("  [Final] {}", text);
                            } else {
                                logger.info("    [Interim] {}", text);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error parsing message: {}", e.getMessage());
                }
            });

            client.setOnTranscript(transcript -> {
                lastTranscriptTime.set(System.currentTimeMillis());
            });

            client.setOnError(error -> {
                logger.error("WebSocket error: {}", error);
            });

            client.setOnClose(code -> {
                logger.info("Connection closed with code: {}", code);
                isRunning.set(false);
            });

            // Set up audio capture
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            // Connect to Deepgram
            client.setOptions(options);
            client.connect().thenAccept(unused -> {
                // Start audio capture thread
                Thread captureThread = new Thread(() -> {
                    byte[] buffer = new byte[CHUNK_SIZE];
                    long audioMetricsTime = System.currentTimeMillis();
                    long totalBytesRead = 0;
                    long totalFramesRead = 0;
                    long nextCaptureTime = System.currentTimeMillis();

                    while (isRunning.get()) {
                        try {
                            int count = line.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                // Calculate RMS value to check audio level
                                double rms = calculateRMS(buffer, count);
                                double db = 20 * Math.log10(rms);
                                
                                // Only send audio if we detect some sound (above -50 dB)
                                if (db > -50) {
                                    client.sendAudio(buffer);
                                    logger.debug("Sent {} bytes of audio, level: {:.1f} dB", count, String.format("%.1f", db));
                                }
                                
                                // Update metrics
                                totalBytesRead += count;
                                totalFramesRead += count / format.getFrameSize();
                                
                                // Log audio metrics every second
                                long now = System.currentTimeMillis();
                                if (now - audioMetricsTime >= 1000) {
                                    double seconds = (now - audioMetricsTime) / 1000.0;
                                    double frameRate = totalFramesRead / seconds;
                                    logger.debug("Audio metrics: {} bytes/s, {} frames ({} fps), RMS: {}, dB: {}", 
                                        totalBytesRead,
                                        totalFramesRead,
                                        String.format("%.1f", frameRate),
                                        String.format("%.2f", rms),
                                        String.format("%.1f", db));
                                    audioMetricsTime = now;
                                    totalBytesRead = 0;
                                    totalFramesRead = 0;
                                }

                                // Control timing
                                nextCaptureTime += CHUNK_DURATION_MS;
                                long sleepTime = nextCaptureTime - System.currentTimeMillis();
                                if (sleepTime > 0) {
                                    Thread.sleep(sleepTime);
                                }
                            } else {
                                logger.warn("No audio data read from microphone");
                                Thread.sleep(10); // Prevent busy waiting
                            }
                        } catch (Exception e) {
                            if (isRunning.get()) {
                                logger.error("Error capturing audio: {}", e.getMessage());
                            }
                            break;
                        }
                    }
                });
                captureThread.start();
            }).exceptionally(e -> {
                logger.error("Connection error: {}", e.getMessage());
                isRunning.set(false);
                return null;
            });

            // Wait for shutdown signal
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("\nShutting down...");
                isRunning.set(false);
                
                // Stop audio capture
                if (line != null) {
                    line.stop();
                    line.close();
                }
                
                // Send final control message and wait briefly for any pending messages
                try {
                    client.sendControlMessage(ControlMessage.createStop());
                    Thread.sleep(500); // Wait for pending messages
                } catch (Exception e) {
                    logger.debug("Error during shutdown: {}", e.getMessage());
                }
                
                // Close WebSocket connection
                client.disconnect();
                
                // Shutdown scheduler
                try {
                    scheduler.shutdown();
                    if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
                
                logger.info("Shutdown complete");
            }));

            // Wait for exit signal
            exitLatch.await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            logger.error("Error in microphone streaming test: {}", e.getMessage(), e);
            System.exit(1);
        } finally {
            scheduler.shutdown();
        }
    }

    /**
     * Calculate the Root Mean Square (RMS) value of audio samples
     */
    private static double calculateRMS(byte[] buffer, int count) {
        long sum = 0;
        // Convert byte pairs to 16-bit samples and calculate sum of squares
        for (int i = 0; i < count - 1; i += 2) {
            int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            sum += sample * sample;
        }
        return Math.sqrt(sum / (count / 2.0));
    }
} 