package ai.deepgram.sdk.pool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefaultPoolMetricsTest {
    private DefaultPoolMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DefaultPoolMetrics();
    }

    @Test
    void initialState_AllMetricsAreZero() {
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(0, metrics.getIdleConnections());
        assertEquals(0, metrics.getTotalConnectionsCreated());
        assertEquals(0, metrics.getTotalConnectionsAcquired());
        assertEquals(0, metrics.getTotalAcquisitionTimeouts());
        assertEquals(0, metrics.getTotalConnectionErrors());
        assertEquals(0.0, metrics.getAverageTimeToFirstTranscript());
        assertEquals(Long.MAX_VALUE, metrics.getMinTimeToFirstTranscript());
        assertEquals(0, metrics.getMaxTimeToFirstTranscript());
        assertEquals(0.0, metrics.getAverageAcquisitionTime());
        assertEquals(0.0, metrics.getAverageUsageTime());
        assertEquals(0.0, metrics.getPoolUtilization());
        assertEquals(0, metrics.getTotalKeepAlivesSent());
        assertEquals(0, metrics.getTotalTimeoutClosures());
    }

    @Test
    void connectionLifecycle_MetricsAreUpdatedCorrectly() {
        // Create new connection
        metrics.incrementActiveConnections();
        assertEquals(1, metrics.getActiveConnections());
        assertEquals(1, metrics.getTotalConnectionsCreated());

        // Connection becomes idle
        metrics.incrementIdleConnections();
        assertEquals(1, metrics.getActiveConnections());
        assertEquals(1, metrics.getIdleConnections());

        // Acquire connection
        metrics.recordConnectionAcquired();
        assertEquals(2, metrics.getActiveConnections());
        assertEquals(0, metrics.getIdleConnections());
        assertEquals(1, metrics.getTotalConnectionsAcquired());

        // Release connection
        metrics.recordConnectionReleased();
        assertEquals(1, metrics.getActiveConnections());
        assertEquals(1, metrics.getIdleConnections());

        // Close connection
        metrics.recordConnectionClosed();
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(1, metrics.getIdleConnections());

        // Close idle connection
        metrics.recordConnectionClosed();
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(0, metrics.getIdleConnections());
    }

    @Test
    void timeToFirstTranscript_MetricsAreUpdatedCorrectly() {
        metrics.recordTimeToFirstTranscript(100);
        assertEquals(100.0, metrics.getAverageTimeToFirstTranscript());
        assertEquals(100, metrics.getMinTimeToFirstTranscript());
        assertEquals(100, metrics.getMaxTimeToFirstTranscript());

        metrics.recordTimeToFirstTranscript(50);
        assertEquals(75.0, metrics.getAverageTimeToFirstTranscript());
        assertEquals(50, metrics.getMinTimeToFirstTranscript());
        assertEquals(100, metrics.getMaxTimeToFirstTranscript());

        metrics.recordTimeToFirstTranscript(200);
        assertEquals(116.666666667, metrics.getAverageTimeToFirstTranscript(), 0.000001);
        assertEquals(50, metrics.getMinTimeToFirstTranscript());
        assertEquals(200, metrics.getMaxTimeToFirstTranscript());
    }

    @Test
    void acquisitionTime_MetricsAreUpdatedCorrectly() {
        metrics.recordAcquisitionTime(100);
        assertEquals(100.0, metrics.getAverageAcquisitionTime());

        metrics.recordAcquisitionTime(200);
        assertEquals(150.0, metrics.getAverageAcquisitionTime());
    }

    @Test
    void usageTime_MetricsAreUpdatedCorrectly() {
        metrics.recordUsageTime(1000);
        assertEquals(1000.0, metrics.getAverageUsageTime());

        metrics.recordUsageTime(2000);
        assertEquals(1500.0, metrics.getAverageUsageTime());
    }

    @Test
    void poolUtilization_CalculatedCorrectly() {
        metrics.incrementActiveConnections(); // 1 active, 0 idle = 100%
        assertEquals(100.0, metrics.getPoolUtilization());

        metrics.incrementIdleConnections(); // 1 active, 1 idle = 50%
        assertEquals(50.0, metrics.getPoolUtilization());

        metrics.recordConnectionAcquired(); // 2 active, 0 idle = 100%
        assertEquals(100.0, metrics.getPoolUtilization());

        metrics.recordConnectionReleased(); // 1 active, 1 idle = 50%
        assertEquals(50.0, metrics.getPoolUtilization());
    }

    @Test
    void errorMetrics_UpdatedCorrectly() {
        metrics.recordError(new RuntimeException("Test error"));
        assertEquals(1, metrics.getTotalConnectionErrors());

        metrics.recordAcquisitionTimeout();
        assertEquals(1, metrics.getTotalAcquisitionTimeouts());
    }

    @Test
    void keepAliveMetrics_UpdatedCorrectly() {
        metrics.recordKeepAliveSent();
        assertEquals(1, metrics.getTotalKeepAlivesSent());

        metrics.recordKeepAliveSent();
        assertEquals(2, metrics.getTotalKeepAlivesSent());
    }

    @Test
    void timeoutMetrics_UpdatedCorrectly() {
        metrics.recordTimeoutClosure();
        assertEquals(1, metrics.getTotalTimeoutClosures());

        metrics.recordTimeoutClosure();
        assertEquals(2, metrics.getTotalTimeoutClosures());
    }
} 