package ai.deepgram.sdk.pool;

import ai.deepgram.sdk.websocket.AudioStreamOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeepgramConnectionPoolTest {
    private static final String TEST_URL = "ws://test.url";
    private static final String TEST_API_KEY = "test_api_key";
    private static final int TEST_INITIAL_SIZE = 3;
    private static final int TEST_MAX_SIZE = 5;
    private static final int TEST_KEEP_ALIVE_INTERVAL = 30000;
    private static final int TEST_CONNECTION_TIMEOUT = 3600000;

    @Mock
    private PoolMetrics mockMetrics;

    private AudioStreamOptions options;
    private PoolConfig poolConfig;
    private DeepgramConnectionPool pool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setChannels(1)
            .setModel("nova-2");

        poolConfig = new PoolConfig()
            .setInitialSize(TEST_INITIAL_SIZE)
            .setMaxSize(TEST_MAX_SIZE)
            .setKeepAliveInterval(TEST_KEEP_ALIVE_INTERVAL)
            .setConnectionTimeout(TEST_CONNECTION_TIMEOUT);

        pool = new DeepgramConnectionPool(TEST_URL, TEST_API_KEY, poolConfig, options);
    }

    @Test
    void constructor_ValidInput_CreatesPool() {
        assertNotNull(pool);
        assertEquals(TEST_INITIAL_SIZE, pool.getIdleCount());
    }

    @Test
    void constructor_NullUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramConnectionPool(null, TEST_API_KEY, poolConfig, options)
        );
    }

    @Test
    void constructor_EmptyUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramConnectionPool("", TEST_API_KEY, poolConfig, options)
        );
    }

    @Test
    void constructor_NullApiKey_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramConnectionPool(TEST_URL, null, poolConfig, options)
        );
    }

    @Test
    void constructor_EmptyApiKey_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramConnectionPool(TEST_URL, "", poolConfig, options)
        );
    }

    @Test
    void constructor_NullConfig_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramConnectionPool(TEST_URL, TEST_API_KEY, null, options)
        );
    }

    @Test
    void constructor_NullOptions_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeepgramConnectionPool(TEST_URL, TEST_API_KEY, poolConfig, null)
        );
    }

    @Test
    void acquire_AvailableConnection_ReturnsConnection() throws Exception {
        PooledDeepgramConnection connection = pool.acquire();
        assertNotNull(connection);
        assertEquals(PooledDeepgramConnection.State.ACTIVE, connection.getState());
        assertEquals(TEST_INITIAL_SIZE - 1, pool.getIdleCount());
        assertEquals(1, pool.getActiveCount());
    }

    @Test
    void acquire_NoAvailableConnections_WaitsForRelease() throws Exception {
        // Acquire all connections
        for (int i = 0; i < TEST_MAX_SIZE; i++) {
            pool.acquire();
        }

        // Next acquire should throw
        assertThrows(TimeoutException.class, () -> pool.acquire());
    }

    @Test
    void close_ClosesAllConnections() throws Exception {
        // Acquire some connections first
        PooledDeepgramConnection conn1 = pool.acquire();
        PooledDeepgramConnection conn2 = pool.acquire();

        // Close the pool
        pool.close();

        // Verify connections are closed
        assertEquals(PooledDeepgramConnection.State.CLOSED, conn1.getState());
        assertEquals(PooledDeepgramConnection.State.CLOSED, conn2.getState());
        assertEquals(0, pool.getIdleCount());
        assertEquals(0, pool.getActiveCount());
    }

    @Test
    void close_AlreadyClosed_ThrowsException() throws Exception {
        pool.close();
        assertThrows(IllegalStateException.class, () -> pool.close());
    }

    @Test
    void acquire_PoolClosed_ThrowsException() throws Exception {
        pool.close();
        assertThrows(IllegalStateException.class, () -> pool.acquire());
    }
} 