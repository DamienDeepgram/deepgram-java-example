package ai.deepgram.sdk.pool;

/**
 * Configuration options for the Deepgram connection pool.
 * Uses the builder pattern for fluent configuration.
 */
public class PoolConfig {
    private static final int DEFAULT_INITIAL_SIZE = 5;
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final int DEFAULT_KEEP_ALIVE_INTERVAL = 30000; // 30 seconds
    private static final int DEFAULT_CONNECTION_TIMEOUT = 3600000; // 60 minutes
    private static final int DEFAULT_ACQUIRE_TIMEOUT = 5000; // 5 seconds
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_RETRY_DELAY = 1000; // 1 second

    private int initialSize;
    private int maxSize;
    private int keepAliveInterval;
    private int connectionTimeout;
    private int acquireTimeout;
    private int maxRetries;
    private int retryDelay;

    /**
     * Creates a new PoolConfig with default values.
     */
    public PoolConfig() {
        this.initialSize = DEFAULT_INITIAL_SIZE;
        this.maxSize = DEFAULT_MAX_SIZE;
        this.keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;
        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        this.acquireTimeout = DEFAULT_ACQUIRE_TIMEOUT;
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.retryDelay = DEFAULT_RETRY_DELAY;
    }

    /**
     * Sets the initial pool size.
     * @param initialSize The number of connections to create initially
     * @return this instance for chaining
     * @throws IllegalArgumentException if initialSize is negative or greater than maxSize
     */
    public PoolConfig setInitialSize(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Initial size cannot be negative");
        }
        if (initialSize > maxSize) {
            throw new IllegalArgumentException("Initial size cannot be greater than max size");
        }
        this.initialSize = initialSize;
        return this;
    }

    /**
     * Sets the maximum pool size.
     * @param maxSize The maximum number of connections in the pool
     * @return this instance for chaining
     * @throws IllegalArgumentException if maxSize is less than initialSize
     */
    public PoolConfig setMaxSize(int maxSize) {
        if (maxSize < initialSize) {
            throw new IllegalArgumentException("Max size cannot be less than initial size");
        }
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Sets the keep-alive interval in milliseconds.
     * @param keepAliveInterval The interval between keep-alive messages
     * @return this instance for chaining
     * @throws IllegalArgumentException if interval is negative
     */
    public PoolConfig setKeepAliveInterval(int keepAliveInterval) {
        if (keepAliveInterval < 0) {
            throw new IllegalArgumentException("Keep-alive interval cannot be negative");
        }
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }

    /**
     * Sets the connection timeout in milliseconds.
     * @param connectionTimeout The time after which idle connections are closed
     * @return this instance for chaining
     * @throws IllegalArgumentException if timeout is negative
     */
    public PoolConfig setConnectionTimeout(int connectionTimeout) {
        if (connectionTimeout < 0) {
            throw new IllegalArgumentException("Connection timeout cannot be negative");
        }
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the acquire timeout in milliseconds.
     * @param acquireTimeout The maximum time to wait for a connection
     * @return this instance for chaining
     * @throws IllegalArgumentException if timeout is negative
     */
    public PoolConfig setAcquireTimeout(int acquireTimeout) {
        if (acquireTimeout < 0) {
            throw new IllegalArgumentException("Acquire timeout cannot be negative");
        }
        this.acquireTimeout = acquireTimeout;
        return this;
    }

    /**
     * Sets the maximum number of connection retries.
     * @param maxRetries The maximum number of retries
     * @return this instance for chaining
     * @throws IllegalArgumentException if maxRetries is negative
     */
    public PoolConfig setMaxRetries(int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Sets the delay between retries in milliseconds.
     * @param retryDelay The delay between retry attempts
     * @return this instance for chaining
     * @throws IllegalArgumentException if delay is negative
     */
    public PoolConfig setRetryDelay(int retryDelay) {
        if (retryDelay < 0) {
            throw new IllegalArgumentException("Retry delay cannot be negative");
        }
        this.retryDelay = retryDelay;
        return this;
    }

    // Getters
    public int getInitialSize() { return initialSize; }
    public int getMaxSize() { return maxSize; }
    public int getKeepAliveInterval() { return keepAliveInterval; }
    public int getConnectionTimeout() { return connectionTimeout; }
    public int getAcquireTimeout() { return acquireTimeout; }
    public int getMaxRetries() { return maxRetries; }
    public int getRetryDelay() { return retryDelay; }
} 