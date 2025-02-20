package ai.deepgram.sdk.pool;

import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import ai.deepgram.sdk.websocket.AudioStreamOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A connection pool for managing multiple Deepgram WebSocket connections.
 * Provides connection pooling, reuse, and automatic cleanup of idle connections.
 */
public class DeepgramConnectionPool implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DeepgramConnectionPool.class);

    private final String url;
    private final String apiKey;
    private final PoolConfig config;
    private final AudioStreamOptions options;
    private final DefaultPoolMetrics metrics;
    private final Queue<PooledDeepgramConnection> idleConnections;
    private final Queue<PooledDeepgramConnection> activeConnections;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isShutdown;

    /**
     * Creates a new DeepgramConnectionPool with the specified configuration.
     *
     * @param url The Deepgram WebSocket URL
     * @param apiKey The Deepgram API key
     * @param config The pool configuration
     * @param options The audio stream options
     */
    public DeepgramConnectionPool(String url, String apiKey, PoolConfig config, AudioStreamOptions options) {
        this.url = url;
        this.apiKey = apiKey;
        this.config = config;
        this.options = options;
        this.metrics = new DefaultPoolMetrics();
        this.idleConnections = new ConcurrentLinkedQueue<>();
        this.activeConnections = new ConcurrentLinkedQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.isShutdown = new AtomicBoolean(false);

        initializePool();
    }

    /**
     * Acquires a connection from the pool.
     * If no idle connection is available and the pool size is below maximum,
     * creates a new connection. Otherwise, waits for a connection to become available.
     *
     * @return A pooled connection ready for use
     * @throws IllegalStateException if the pool is shutdown
     * @throws TimeoutException if no connection becomes available within the timeout
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public PooledDeepgramConnection acquire() throws TimeoutException, InterruptedException {
        if (isShutdown.get()) {
            throw new IllegalStateException("Connection pool is shutdown");
        }

        long startTime = System.currentTimeMillis();
        PooledDeepgramConnection connection = null;
        long remainingTimeout = config.getAcquireTimeout();

        while (remainingTimeout > 0) {
            connection = idleConnections.poll();
            if (connection != null) {
                if (connection.getState() == PooledDeepgramConnection.State.CLOSED) {
                    // Skip closed connections and try again
                    continue;
                }
                break;
            }

            if (getTotalConnections() < config.getMaxSize()) {
                connection = createConnection();
                break;
            }

            // Wait for a connection to become available
            Thread.sleep(Math.min(100, remainingTimeout));
            remainingTimeout = config.getAcquireTimeout() - (System.currentTimeMillis() - startTime);
        }

        if (connection == null) {
            metrics.recordAcquisitionTimeout();
            throw new TimeoutException("Failed to acquire connection within timeout");
        }

        try {
            connection.activate();
            activeConnections.offer(connection);
            metrics.recordAcquisitionTime(System.currentTimeMillis() - startTime);
            return connection;
        } catch (Exception e) {
            idleConnections.offer(connection);
            throw e;
        }
    }

    /**
     * Releases a connection back to the pool.
     *
     * @param connection The connection to release
     * @throws IllegalStateException if the connection is not from this pool
     */
    public void release(PooledDeepgramConnection connection) {
        if (!activeConnections.remove(connection)) {
            throw new IllegalStateException("Connection is not from this pool");
        }

        try {
            connection.release();
            idleConnections.offer(connection);
        } catch (Exception e) {
            logger.error("Error releasing connection", e);
            connection.close();
        }
    }

    /**
     * Gets the current number of idle connections.
     * @return The number of idle connections
     */
    public int getIdleCount() {
        return idleConnections.size();
    }

    /**
     * Gets the current number of active connections.
     * @return The number of active connections
     */
    public int getActiveCount() {
        return activeConnections.size();
    }

    /**
     * Gets the total number of connections in the pool.
     * @return The total number of connections
     */
    public int getTotalConnections() {
        return getIdleCount() + getActiveCount();
    }

    /**
     * Gets the pool metrics.
     * @return The pool metrics
     */
    public PoolMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void close() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Shutting down connection pool");
            scheduler.shutdown();
            
            // Close all connections
            for (PooledDeepgramConnection conn : idleConnections) {
                conn.close();
            }
            for (PooledDeepgramConnection conn : activeConnections) {
                conn.close();
            }
            
            idleConnections.clear();
            activeConnections.clear();

            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }
    }

    /**
     * Initializes the connection pool with the initial number of connections.
     */
    private void initializePool() {
        for (int i = 0; i < config.getInitialSize(); i++) {
            try {
                PooledDeepgramConnection connection = createConnection();
                idleConnections.offer(connection);
            } catch (Exception e) {
                logger.error("Error creating initial connection", e);
                metrics.recordError(e);
            }
        }
    }

    /**
     * Creates a new pooled connection.
     *
     * @return A new pooled connection
     * @throws RuntimeException if connection creation fails
     */
    private PooledDeepgramConnection createConnection() {
        try {
            DeepgramWebSocket connection = new DeepgramWebSocket(url, apiKey);
            
            // Apply audio stream options if provided
            if (options != null) {
                connection.setOptions(options);
            }

            // Set up basic error and close handlers
            connection.setOnError(error -> {
                logger.error("Connection error during initialization: {}", error);
            });

            connection.setOnClose(code -> {
                logger.debug("Connection closed during initialization with code: {}", code);
            });
            
            // Connect with timeout
            connection.connect().get(config.getAcquireTimeout(), TimeUnit.MILLISECONDS);

            PooledDeepgramConnection pooledConnection = new PooledDeepgramConnection(
                connection,
                metrics,
                scheduler,
                config.getKeepAliveInterval(),
                config.getConnectionTimeout()
            );

            ((DefaultPoolMetrics)metrics).incrementActiveConnections();
            return pooledConnection;
        } catch (Exception e) {
            metrics.recordError(e);
            throw new RuntimeException("Failed to create connection", e);
        }
    }
} 