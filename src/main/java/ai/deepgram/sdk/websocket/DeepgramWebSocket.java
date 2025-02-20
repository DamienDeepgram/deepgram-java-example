package ai.deepgram.sdk.websocket;

import ai.deepgram.sdk.message.ControlMessage;
import ai.deepgram.sdk.message.TranscriptMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;

/**
 * WebSocket client for connecting to Deepgram's real-time transcription API.
 */
public class DeepgramWebSocket {
    private static final Logger logger = LoggerFactory.getLogger(DeepgramWebSocket.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int PING_INTERVAL_MS = 30000;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int MAX_RETRIES = 3;

    private String url;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private WebSocket webSocket;
    private Consumer<TranscriptMessage> onTranscript;
    private Consumer<String> onError;
    private Consumer<Integer> onClose;
    private Consumer<Void> onOpen;
    private Consumer<String> onRawMessage;
    private boolean isConnected = false;
    private CountDownLatch connectionLatch;
    private final AtomicLong startTime = new AtomicLong(0);

    /**
     * Creates a new DeepgramWebSocket instance.
     * @param url The WebSocket URL
     * @param apiKey The Deepgram API key
     */
    public DeepgramWebSocket(String url, String apiKey) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }

        this.url = url;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sets the transcript event handler.
     * @param onTranscript The handler to call when a transcript is received
     */
    public void setOnTranscript(Consumer<TranscriptMessage> onTranscript) {
        this.onTranscript = onTranscript;
    }

    /**
     * Sets the error event handler.
     * @param onError The handler to call when an error occurs
     */
    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    /**
     * Sets the close event handler.
     * @param onClose The handler to call when the connection is closed
     */
    public void setOnClose(Consumer<Integer> onClose) {
        this.onClose = onClose;
    }

    /**
     * Sets the open event handler.
     * @param onOpen The handler to call when the connection is opened
     */
    public void setOnOpen(Consumer<Void> onOpen) {
        this.onOpen = onOpen;
    }

    /**
     * Sets the raw message handler to receive all WebSocket messages before processing.
     * @param onRawMessage The handler to call when any message is received
     */
    public void setOnRawMessage(Consumer<String> onRawMessage) {
        this.onRawMessage = onRawMessage;
    }

    /**
     * Sets the audio stream options.
     * @param options The options to use for the stream
     */
    public void setOptions(AudioStreamOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Options cannot be null");
        }
        this.url = options.appendToUrl(this.url);
    }

    /**
     * Connects to the Deepgram WebSocket API.
     * @return A future that completes when the connection is established
     */
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        connectionLatch = new CountDownLatch(1);

        try {
            startTime.set(System.currentTimeMillis());
            webSocket = new WebSocketFactory()
                .setConnectionTimeout(CONNECT_TIMEOUT_MS)
                .createSocket(url)
                .addHeader("Authorization", "Token " + apiKey)
                .setPingInterval(PING_INTERVAL_MS)
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                        logger.debug("Connected to Deepgram WebSocket API");
                        isConnected = true;
                        connectionLatch.countDown();
                        if (onOpen != null) {
                            onOpen.accept(null);
                        }
                        future.complete(null);
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) {
                        // Call raw message handler first if set
                        if (onRawMessage != null) {
                            onRawMessage.accept(text);
                        }
                        
                        try {
                            logger.debug("Received message: {}", text);
                            TranscriptResponse response = objectMapper.readValue(text, TranscriptResponse.class);
                            TranscriptMessage message = response.toMessage();
                            if (onTranscript != null && message != null) {
                                onTranscript.accept(message);
                            } else {
                                logger.debug("No transcript handler or empty message");
                            }
                        } catch (IOException e) {
                            logger.error("Error parsing transcript response: {}", e.getMessage());
                            if (onError != null) {
                                onError.accept("Error parsing transcript response: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) {
                        logger.error("WebSocket error", cause);
                        if (onError != null) {
                            onError.accept(cause.getMessage());
                        }
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket,
                                             WebSocketFrame serverCloseFrame,
                                             WebSocketFrame clientCloseFrame,
                                             boolean closedByServer) {
                        logger.debug("Disconnected from Deepgram WebSocket API");
                        isConnected = false;
                        if (onClose != null) {
                            int code = serverCloseFrame != null ? serverCloseFrame.getCloseCode() :
                                     clientCloseFrame != null ? clientCloseFrame.getCloseCode() : 
                                     1006;
                            onClose.accept(code);
                        }
                    }
                })
                .connect();
        } catch (IOException | WebSocketException e) {
            logger.error("Error connecting to Deepgram WebSocket API", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Disconnects from the Deepgram WebSocket API.
     */
    public void disconnect() {
        if (webSocket != null) {
            try {
                webSocket.disconnect();
            } catch (Exception e) {
                logger.error("Error disconnecting from Deepgram WebSocket API", e);
            }
        }
        isConnected = false;
    }

    /**
     * Sends audio data to the Deepgram WebSocket API.
     * @param audioData The audio data to send
     */
    public void sendAudio(byte[] audioData) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to Deepgram WebSocket API");
        }
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("Audio data cannot be null or empty");
        }
        webSocket.sendBinary(audioData);
    }

    /**
     * Checks if the connection is established.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Sends a control message to the Deepgram WebSocket API.
     * @param message The control message to send
     */
    public void sendControlMessage(ControlMessage message) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to Deepgram WebSocket API");
        }
        try {
            // Create a temporary map to convert enum to string value
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("type", message.getControlType().getValue());
            if (message.getMessage() != null) {
                jsonMap.put("message", message.getMessage());
            }
            if (message.getCode() != null) {
                jsonMap.put("code", message.getCode());
            }
            if (message.getDetails() != null) {
                jsonMap.put("details", message.getDetails());
            }
            String json = objectMapper.writeValueAsString(jsonMap);
            webSocket.sendText(json);
        } catch (IOException e) {
            logger.error("Error sending control message", e);
            if (onError != null) {
                onError.accept("Error sending control message: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a keep-alive message to maintain the WebSocket connection.
     */
    public void sendKeepAlive() {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to Deepgram WebSocket API");
        }
        sendControlMessage(ControlMessage.createKeepalive());
    }

    public long getStartTime() {
        return startTime.get();
    }
}