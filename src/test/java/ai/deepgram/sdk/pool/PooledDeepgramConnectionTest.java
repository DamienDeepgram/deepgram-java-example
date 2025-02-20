package ai.deepgram.sdk.pool;

import ai.deepgram.sdk.message.ControlMessage;
import ai.deepgram.sdk.websocket.AudioStreamOptions;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PooledDeepgramConnectionTest {
    private static final String TEST_URL = "ws://test.url";
    private static final String TEST_API_KEY = "test_api_key";

    @Mock
    private DeepgramWebSocket mockWebSocket;
    @Mock
    private PoolMetrics mockMetrics;
    @Mock
    private ScheduledExecutorService mockExecutor;
    @Mock
    private ScheduledFuture<?> mockFuture;

    private AudioStreamOptions options;
    private PooledDeepgramConnection connection;
    private static final int KEEP_ALIVE_INTERVAL = 30000;
    private static final int CONNECTION_TIMEOUT = 3600000;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setChannels(1)
            .setModel("nova-2");

        doReturn(mockFuture).when(mockExecutor).scheduleAtFixedRate(
            any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)
        );

        connection = new PooledDeepgramConnection(mockWebSocket, mockMetrics, mockExecutor, KEEP_ALIVE_INTERVAL, CONNECTION_TIMEOUT);
    }

    @Test
    void constructor_InitializesCorrectly() {
        assertEquals(PooledDeepgramConnection.State.IDLE, connection.getState());
        verify(mockExecutor, times(2)).scheduleAtFixedRate(
            any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void activate_FromIdleState_Succeeds() {
        when(mockWebSocket.isConnected()).thenReturn(false);
        connection.activate();
        assertEquals(PooledDeepgramConnection.State.ACTIVE, connection.getState());
        verify(mockWebSocket).connect();
        verify(mockMetrics).recordConnectionAcquired();
    }

    @Test
    void activate_AlreadyConnected_DoesNotReconnect() {
        when(mockWebSocket.isConnected()).thenReturn(true);
        connection.activate();
        assertEquals(PooledDeepgramConnection.State.ACTIVE, connection.getState());
        verify(mockWebSocket, never()).connect();
        verify(mockMetrics).recordConnectionAcquired();
    }

    @Test
    void activate_FromActiveState_ThrowsException() {
        when(mockWebSocket.isConnected()).thenReturn(true);
        connection.activate();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            connection.activate()
        );
        assertEquals("Connection is not in IDLE state", exception.getMessage());
    }

    @Test
    void release_FromActiveState_Succeeds() {
        connection.activate();
        connection.release();
        assertEquals(PooledDeepgramConnection.State.IDLE, connection.getState());
        verify(mockMetrics).recordConnectionReleased();
    }

    @Test
    void release_FromIdleState_ThrowsException() {
        assertThrows(IllegalStateException.class, () -> connection.release());
    }

    @Test
    void sendAudio_NotActive_ThrowsException() {
        when(mockWebSocket.isConnected()).thenReturn(true);
        byte[] testData = {1, 2, 3};

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            connection.sendAudio(testData)
        );
        assertEquals("Connection is not in ACTIVE state", exception.getMessage());
        verify(mockWebSocket, never()).sendAudio(any());
    }

    @Test
    void sendAudio_ActiveAndConnected_SendsData() {
        when(mockWebSocket.isConnected()).thenReturn(true);
        connection.activate();
        byte[] testData = {1, 2, 3};

        connection.sendAudio(testData);

        verify(mockWebSocket).sendAudio(testData);
    }

    @Test
    void sendAudio_ActiveButNotConnected_ThrowsException() {
        when(mockWebSocket.isConnected()).thenReturn(false);
        connection.activate();
        byte[] testData = {1, 2, 3};

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            connection.sendAudio(testData)
        );
        assertEquals("Connection is not established", exception.getMessage());
        verify(mockWebSocket, never()).sendAudio(any());
    }

    @Test
    void close_CancelsHeartbeatAndDisconnects() {
        connection.close();

        verify(mockFuture, times(2)).cancel(false);
        verify(mockWebSocket).disconnect();
        assertEquals(PooledDeepgramConnection.State.CLOSED, connection.getState());
    }

    @Test
    void onError_ClosesConnectionAndRecordsError() {
        ArgumentCaptor<Consumer<String>> errorHandlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockWebSocket).setOnError(errorHandlerCaptor.capture());

        errorHandlerCaptor.getValue().accept("Test error");
        assertEquals(PooledDeepgramConnection.State.CLOSED, connection.getState());
        verify(mockMetrics).recordError(any(RuntimeException.class));
    }

    @Test
    void onClose_ClosesConnection() {
        ArgumentCaptor<Consumer<Integer>> closeHandlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockWebSocket).setOnClose(closeHandlerCaptor.capture());

        closeHandlerCaptor.getValue().accept(1000);
        assertEquals(PooledDeepgramConnection.State.CLOSED, connection.getState());
    }

    @Test
    void keepAlive_SendsMessage() {
        ArgumentCaptor<Runnable> keepAliveCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockExecutor).scheduleAtFixedRate(
            keepAliveCaptor.capture(),
            eq((long) KEEP_ALIVE_INTERVAL),
            eq((long) KEEP_ALIVE_INTERVAL),
            eq(TimeUnit.MILLISECONDS)
        );

        keepAliveCaptor.getValue().run();
        verify(mockWebSocket).sendControlMessage(any(ControlMessage.class));
        verify(mockMetrics).recordKeepAliveSent();
    }

    @Test
    void timeout_ClosesIdleConnection() {
        ArgumentCaptor<Runnable> timeoutCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockExecutor).scheduleAtFixedRate(
            timeoutCaptor.capture(),
            eq((long) CONNECTION_TIMEOUT),
            eq((long) CONNECTION_TIMEOUT),
            eq(TimeUnit.MILLISECONDS)
        );

        // Simulate connection being idle for longer than timeout
        timeoutCaptor.getValue().run();
        verify(mockMetrics).recordTimeoutClosure();
        assertEquals(PooledDeepgramConnection.State.CLOSED, connection.getState());
    }
} 