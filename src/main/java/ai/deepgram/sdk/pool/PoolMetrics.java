package ai.deepgram.sdk.pool;

/**
 * Interface defining metrics collection for the connection pool.
 */
public interface PoolMetrics {
    /**
     * Gets the current number of active connections.
     * @return The number of active connections
     */
    int getActiveConnections();

    /**
     * Gets the current number of idle connections.
     * @return The number of idle connections
     */
    int getIdleConnections();

    /**
     * Gets the total number of connections created since pool initialization.
     * @return The total connections created
     */
    long getTotalConnectionsCreated();

    /**
     * Gets the total number of connections acquired from the pool.
     * @return The total connections acquired
     */
    long getTotalConnectionsAcquired();

    /**
     * Gets the total number of connection acquisition timeouts.
     * @return The total acquisition timeouts
     */
    long getTotalAcquisitionTimeouts();

    /**
     * Gets the total number of connection errors.
     * @return The total connection errors
     */
    long getTotalConnectionErrors();

    /**
     * Gets the average time to first transcript in milliseconds.
     * @return The average time to first transcript
     */
    double getAverageTimeToFirstTranscript();

    /**
     * Gets the minimum time to first transcript in milliseconds.
     * @return The minimum time to first transcript
     */
    long getMinTimeToFirstTranscript();

    /**
     * Gets the maximum time to first transcript in milliseconds.
     * @return The maximum time to first transcript
     */
    long getMaxTimeToFirstTranscript();

    /**
     * Gets the average connection acquisition time in milliseconds.
     * @return The average acquisition time
     */
    double getAverageAcquisitionTime();

    /**
     * Gets the average connection usage time in milliseconds.
     * @return The average usage time
     */
    double getAverageUsageTime();

    /**
     * Gets the current pool utilization as a percentage (0-100).
     * @return The pool utilization percentage
     */
    double getPoolUtilization();

    /**
     * Gets the total number of keep-alive messages sent.
     * @return The total keep-alive messages
     */
    long getTotalKeepAlivesSent();

    /**
     * Gets the total number of connections closed due to timeout.
     * @return The total timeout closures
     */
    long getTotalTimeoutClosures();

    /**
     * Records a new time to first transcript measurement.
     * @param timeMs The time in milliseconds
     */
    void recordTimeToFirstTranscript(long timeMs);

    /**
     * Records a new connection acquisition time measurement.
     * @param timeMs The time in milliseconds
     */
    void recordAcquisitionTime(long timeMs);

    /**
     * Records a new connection usage time measurement.
     * @param timeMs The time in milliseconds
     */
    void recordUsageTime(long timeMs);

    /**
     * Records a connection error.
     * @param error The error that occurred
     */
    void recordError(Throwable error);

    /**
     * Records a keep-alive message being sent.
     */
    void recordKeepAliveSent();

    /**
     * Records a connection being closed due to timeout.
     */
    void recordTimeoutClosure();

    /**
     * Records a connection being acquired from the pool.
     */
    void recordConnectionAcquired();

    /**
     * Records a connection being released back to the pool.
     */
    void recordConnectionReleased();

    /**
     * Records a connection being closed.
     */
    void recordConnectionClosed();

    /**
     * Records a connection acquisition timeout.
     */
    void recordAcquisitionTimeout();
} 