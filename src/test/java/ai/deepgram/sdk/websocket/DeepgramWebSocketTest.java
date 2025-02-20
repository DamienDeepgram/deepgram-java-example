package ai.deepgram.sdk.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DeepgramWebSocketTest {
    private static final Logger logger = LoggerFactory.getLogger(DeepgramWebSocketTest.class);
    private static final int TEST_PORT = 8025;
    private static final String TEST_URL = "ws://localhost:" + TEST_PORT;
    private static final String TEST_API_KEY = "test_api_key";
    private MockWebSocketServer mockServer;
    private DeepgramWebSocket client;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebSocketServer(TEST_PORT);
        mockServer.startAndWait();
        client = new DeepgramWebSocket(TEST_URL, TEST_API_KEY);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.disconnect();
        }
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    private CompletableFuture<Void> connectClient() throws Exception {
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setChannels(1)
            .setModel("nova-2");
        client.setOptions(options);
        return client.connect();
    }

    @Test
    void constructor_NullApiKey_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramWebSocket(TEST_URL, null)
        );
    }

    @Test
    void constructor_EmptyApiKey_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramWebSocket(TEST_URL, "")
        );
    }

    @Test
    void constructor_NullBaseUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramWebSocket(null, TEST_API_KEY)
        );
    }

    @Test
    void constructor_EmptyBaseUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramWebSocket("", TEST_API_KEY)
        );
    }

    @Test
    void setOptions_NullOptions_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            client.setOptions(null)
        );
    }

    @Test
    void sendAudio_NotConnected_ThrowsException() {
        assertThrows(IllegalStateException.class, () ->
            client.sendAudio(new byte[]{1, 2, 3})
        );
    }

    @Test
    void sendAudio_NullData_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            client.sendAudio(null)
        );
    }

    @Test
    void sendAudio_EmptyData_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            client.sendAudio(new byte[0])
        );
    }

    @Test
    @Timeout(5)
    void connect_ValidOptions_EstablishesConnection() throws Exception {
        mockServer.setOnConnect(conn -> {
            logger.info("Mock server received connection");
        });

        CompletableFuture<Void> future = connectClient();
        future.get(5, TimeUnit.SECONDS);

        assertTrue(client.isConnected());
        assertTrue(mockServer.awaitConnection(5, TimeUnit.SECONDS));
    }

    @Test
    @Timeout(5)
    void disconnect_ClosesConnection() throws Exception {
        mockServer.setOnConnect(conn -> {
            logger.info("Mock server received connection");
        });

        mockServer.setOnClose(conn -> {
            logger.info("Mock server connection closed");
        });

        CompletableFuture<Void> future = connectClient();
        future.get(5, TimeUnit.SECONDS);

        assertTrue(client.isConnected());
        client.disconnect();
        assertFalse(client.isConnected());
    }

    @Test
    @Timeout(30)
    void reconnect_AttemptsReconnectionOnFailure() throws Exception {
        mockServer.setOnConnect(conn -> {
            logger.info("Mock server received connection");
        });

        mockServer.setOnClose(conn -> {
            logger.info("Mock server connection closed");
        });

        mockServer.setOnMessage(message -> {
            logger.info("Mock server received message: {}", message);
        });

        CompletableFuture<Void> future = connectClient();
        future.get(5, TimeUnit.SECONDS);

        assertTrue(client.isConnected());

        // Simulate server-side disconnect
        mockServer.closeAllConnectionsWithCode(1006, "Connection reset");

        // Wait for reconnection
        Thread.sleep(2000);

        assertTrue(client.isConnected());
    }

    @Test
    @Timeout(5)
    void onTranscript_HandlerReceivesMessages() throws Exception {
        CompletableFuture<String> transcriptFuture = new CompletableFuture<>();

        client.setOnTranscript(message -> {
            logger.info("Received transcript: {}", message.getTranscript());
            transcriptFuture.complete(message.getTranscript());
        });

        CompletableFuture<Void> connectFuture = connectClient();
        connectFuture.get(5, TimeUnit.SECONDS);

        String testTranscript = "{\"type\":\"Results\",\"channel\":{\"alternatives\":[{\"transcript\":\"test\",\"confidence\":0.9,\"words\":[]}]}}";
        mockServer.broadcast(testTranscript);

        String receivedTranscript = transcriptFuture.get(5, TimeUnit.SECONDS);
        assertEquals("test", receivedTranscript);
    }
} 