package ai.deepgram.sdk.pool;

import ai.deepgram.sdk.message.ControlMessage;
import ai.deepgram.sdk.websocket.DeepgramWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A wrapper for a DeepgramWebSocket connection that manages its lifecycle within a connection pool.
 */
public class PooledDeepgramConnection {
    private static final Logger logger = LoggerFactory.getLogger(PooledDeepgramConnection.class);

    /**
     * Connection states
     */
    public enum State {
        IDLE,       // Connection is in the pool, not being used
        ACTIVE,     // Connection is being used for streaming
        CLOSED      // Connection is closed and should be removed from the pool
    }

    private final DeepgramWebSocket webSocket;
    private final PoolMetrics metrics;
    private final ScheduledExecutorService executor;
    private final int keepAliveInterval;
    private final int connectionTimeout;
    private final AtomicReference<State> state;
    private final AtomicLong lastUsedTime;
    private final ScheduledFuture<?> keepAliveFuture;
    private final ScheduledFuture<?> timeoutFuture;

    /**
     * Creates a new PooledDeepgramConnection.
     *
     * @param webSocket The DeepgramWebSocket instance to wrap
     * @param metrics The metrics collector
     * @param executor The scheduler for keep-alive and timeout tasks
     * @param keepAliveInterval The interval between keep-alive messages in milliseconds
     * @param connectionTimeout The timeout after which idle connections are closed in milliseconds
     */
    public PooledDeepgramConnection(DeepgramWebSocket webSocket, PoolMetrics metrics,
                                  ScheduledExecutorService executor,
                                  int keepAliveInterval, int connectionTimeout) {
        this.webSocket = webSocket;
        this.metrics = metrics;
        this.executor = executor;
        this.keepAliveInterval = keepAliveInterval;
        this.connectionTimeout = connectionTimeout;
        this.state = new AtomicReference<>(State.IDLE);
        this.lastUsedTime = new AtomicLong(System.currentTimeMillis());

        // Set up error and close handlers
        webSocket.setOnError(error -> {
            logger.error("Connection error: {}", error);
            metrics.recordError(new RuntimeException(error));
            close();
        });

        webSocket.setOnClose(code -> {
            logger.info("Connection closed with code: {}", code);
            close();
        });

        // Schedule keep-alive and timeout tasks
        this.keepAliveFuture = executor.scheduleAtFixedRate(
            this::sendKeepAlive,
            keepAliveInterval,
            keepAliveInterval,
            TimeUnit.MILLISECONDS
        );

        this.timeoutFuture = executor.scheduleAtFixedRate(
            this::checkTimeout,
            connectionTimeout,
            connectionTimeout,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Gets the current state of the connection.
     * @return The connection state
     */
    public State getState() {
        return state.get();
    }

    /**
     * Activates the connection for use.
     * @throws IllegalStateException if the connection is not in IDLE state
     */
    public void activate() {
        if (!state.compareAndSet(State.IDLE, State.ACTIVE)) {
            throw new IllegalStateException("Connection is not in IDLE state");
        }
        if (!webSocket.isConnected()) {
            webSocket.connect();
        }
        lastUsedTime.set(System.currentTimeMillis());
        metrics.recordConnectionAcquired();
    }

    /**
     * Returns the connection to the idle state.
     * @throws IllegalStateException if the connection is not in ACTIVE state
     */
    public void release() {
        if (!state.compareAndSet(State.ACTIVE, State.IDLE)) {
            throw new IllegalStateException("Connection is not in ACTIVE state");
        }
        lastUsedTime.set(System.currentTimeMillis());
        metrics.recordConnectionReleased();
    }

    /**
     * Closes the connection and cancels all scheduled tasks.
     */
    public void close() {
        if (state.getAndSet(State.CLOSED) != State.CLOSED) {
            keepAliveFuture.cancel(false);
            timeoutFuture.cancel(false);
            webSocket.disconnect();
            metrics.recordConnectionClosed();
        }
    }

    /**
     * Sends audio data through the connection.
     * @param data The audio data to send
     * @throws IllegalStateException if the connection is not in ACTIVE state or not established
     */
    public void sendAudio(byte[] data) {
        if (state.get() != State.ACTIVE) {
            throw new IllegalStateException("Connection is not in ACTIVE state");
        }
        if (!webSocket.isConnected()) {
            throw new IllegalStateException("Connection is not established");
        }
        webSocket.sendAudio(data);
        lastUsedTime.set(System.currentTimeMillis());
    }

    private void sendKeepAlive() {
        try {
            webSocket.sendControlMessage(ControlMessage.createKeepalive());
            metrics.recordKeepAliveSent();
        } catch (Exception e) {
            metrics.recordError(e);
            close();
        }
    }

    private void checkTimeout() {
        if (state.get() == State.IDLE) {
            long idleTime = System.currentTimeMillis() - lastUsedTime.get();
            if (idleTime >= connectionTimeout) {
                logger.info("Closing idle connection after {} ms", idleTime);
                metrics.recordTimeoutClosure();
                close();
            }
        }
    }

    public DeepgramWebSocket getConnection() {
        return webSocket;
    }

    /**
     * Gets the executor service used by this connection.
     * @return The scheduler executor service
     */
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Gets the keep-alive interval in milliseconds.
     * @return The keep-alive interval
     */
    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }
} 