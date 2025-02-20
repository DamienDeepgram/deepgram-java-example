package ai.deepgram.sdk.pool;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default implementation of PoolMetrics interface.
 * Thread-safe metrics collection for the connection pool.
 */
public class DefaultPoolMetrics implements PoolMetrics {
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger idleConnections = new AtomicInteger(0);
    private final LongAdder totalConnectionsCreated = new LongAdder();
    private final LongAdder totalConnectionsAcquired = new LongAdder();
    private final LongAdder totalAcquisitionTimeouts = new LongAdder();
    private final LongAdder totalConnectionErrors = new LongAdder();
    private final LongAdder totalKeepAlivesSent = new LongAdder();
    private final LongAdder totalTimeoutClosures = new LongAdder();

    // Time to first transcript metrics
    private final AtomicLong minTimeToFirstTranscript = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxTimeToFirstTranscript = new AtomicLong(0);
    private final LongAdder totalTimeToFirstTranscript = new LongAdder();
    private final LongAdder timeToFirstTranscriptCount = new LongAdder();

    // Acquisition time metrics
    private final AtomicLong totalAcquisitionTime = new AtomicLong(0);
    private final AtomicLong acquisitionCount = new AtomicLong(0);

    // Usage time metrics
    private final LongAdder totalUsageTime = new LongAdder();
    private final LongAdder usageTimeCount = new LongAdder();

    @Override
    public int getActiveConnections() {
        return activeConnections.get();
    }

    @Override
    public int getIdleConnections() {
        return idleConnections.get();
    }

    @Override
    public long getTotalConnectionsCreated() {
        return totalConnectionsCreated.sum();
    }

    @Override
    public long getTotalConnectionsAcquired() {
        return totalConnectionsAcquired.sum();
    }

    @Override
    public long getTotalAcquisitionTimeouts() {
        return totalAcquisitionTimeouts.sum();
    }

    @Override
    public long getTotalConnectionErrors() {
        return totalConnectionErrors.sum();
    }

    @Override
    public double getAverageTimeToFirstTranscript() {
        long count = timeToFirstTranscriptCount.sum();
        return count > 0 ? (double) totalTimeToFirstTranscript.sum() / count : 0.0;
    }

    @Override
    public long getMinTimeToFirstTranscript() {
        return minTimeToFirstTranscript.get();
    }

    @Override
    public long getMaxTimeToFirstTranscript() {
        return maxTimeToFirstTranscript.get();
    }

    @Override
    public double getAverageAcquisitionTime() {
        long count = acquisitionCount.get();
        return count > 0 ? (double) totalAcquisitionTime.get() / count : 0.0;
    }

    @Override
    public double getAverageUsageTime() {
        long count = usageTimeCount.sum();
        return count > 0 ? (double) totalUsageTime.sum() / count : 0.0;
    }

    @Override
    public double getPoolUtilization() {
        int total = activeConnections.get() + idleConnections.get();
        return total > 0 ? (activeConnections.get() * 100.0) / total : 0.0;
    }

    @Override
    public long getTotalKeepAlivesSent() {
        return totalKeepAlivesSent.sum();
    }

    @Override
    public long getTotalTimeoutClosures() {
        return totalTimeoutClosures.sum();
    }

    @Override
    public void recordTimeToFirstTranscript(long timeMs) {
        totalTimeToFirstTranscript.add(timeMs);
        timeToFirstTranscriptCount.increment();
        updateMin(minTimeToFirstTranscript, timeMs);
        updateMax(maxTimeToFirstTranscript, timeMs);
    }

    @Override
    public void recordAcquisitionTime(long milliseconds) {
        totalAcquisitionTime.addAndGet(milliseconds);
        acquisitionCount.incrementAndGet();
    }

    @Override
    public void recordUsageTime(long timeMs) {
        totalUsageTime.add(timeMs);
        usageTimeCount.increment();
    }

    @Override
    public void recordError(Throwable error) {
        totalConnectionErrors.increment();
    }

    @Override
    public void recordKeepAliveSent() {
        totalKeepAlivesSent.increment();
    }

    @Override
    public void recordTimeoutClosure() {
        totalTimeoutClosures.increment();
    }

    /**
     * Records a new active connection.
     */
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
        totalConnectionsCreated.increment();
    }

    /**
     * Records a connection becoming idle.
     */
    public void incrementIdleConnections() {
        idleConnections.incrementAndGet();
    }

    /**
     * Records a connection being acquired from the pool.
     */
    public void recordConnectionAcquired() {
        activeConnections.incrementAndGet();
        idleConnections.decrementAndGet();
        totalConnectionsAcquired.increment();
    }

    /**
     * Records a connection being released back to the pool.
     */
    public void recordConnectionReleased() {
        activeConnections.decrementAndGet();
        idleConnections.incrementAndGet();
    }

    /**
     * Records a connection being removed from the pool.
     */
    public void recordConnectionClosed() {
        int currentActive = activeConnections.get();
        int currentIdle = idleConnections.get();
        
        if (currentActive > 0) {
            activeConnections.decrementAndGet();
        } else if (currentIdle > 0) {
            idleConnections.decrementAndGet();
        }
    }

    /**
     * Records a connection acquisition timeout.
     */
    public void recordAcquisitionTimeout() {
        totalAcquisitionTimeouts.increment();
    }

    private void updateMin(AtomicLong minValue, long newValue) {
        long currentMin;
        do {
            currentMin = minValue.get();
            if (newValue >= currentMin) {
                return;
            }
        } while (!minValue.compareAndSet(currentMin, newValue));
    }

    private void updateMax(AtomicLong maxValue, long newValue) {
        long currentMax;
        do {
            currentMax = maxValue.get();
            if (newValue <= currentMax) {
                return;
            }
        } while (!maxValue.compareAndSet(currentMax, newValue));
    }
} 