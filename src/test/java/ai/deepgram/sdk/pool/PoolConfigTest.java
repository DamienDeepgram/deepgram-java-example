package ai.deepgram.sdk.pool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoolConfigTest {

    @Test
    void constructor_CreatesDefaultConfig() {
        PoolConfig config = new PoolConfig();
        assertEquals(5, config.getInitialSize());
        assertEquals(10, config.getMaxSize());
        assertEquals(30000, config.getKeepAliveInterval());
        assertEquals(3600000, config.getConnectionTimeout());
        assertEquals(5000, config.getAcquireTimeout());
        assertEquals(3, config.getMaxRetries());
        assertEquals(1000, config.getRetryDelay());
    }

    @Test
    void setInitialSize_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setInitialSize(3);
        assertEquals(3, config.getInitialSize());
    }

    @Test
    void setInitialSize_NegativeValue_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setInitialSize(-1));
    }

    @Test
    void setInitialSize_GreaterThanMaxSize_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setInitialSize(11));
    }

    @Test
    void setMaxSize_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setMaxSize(15);
        assertEquals(15, config.getMaxSize());
    }

    @Test
    void setMaxSize_LessThanInitialSize_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setMaxSize(4));
    }

    @Test
    void setKeepAliveInterval_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setKeepAliveInterval(20000);
        assertEquals(20000, config.getKeepAliveInterval());
    }

    @Test
    void setKeepAliveInterval_NegativeValue_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setKeepAliveInterval(-1));
    }

    @Test
    void setConnectionTimeout_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setConnectionTimeout(1800000);
        assertEquals(1800000, config.getConnectionTimeout());
    }

    @Test
    void setConnectionTimeout_NegativeValue_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setConnectionTimeout(-1));
    }

    @Test
    void setAcquireTimeout_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setAcquireTimeout(10000);
        assertEquals(10000, config.getAcquireTimeout());
    }

    @Test
    void setAcquireTimeout_NegativeValue_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setAcquireTimeout(-1));
    }

    @Test
    void setMaxRetries_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setMaxRetries(5);
        assertEquals(5, config.getMaxRetries());
    }

    @Test
    void setMaxRetries_NegativeValue_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setMaxRetries(-1));
    }

    @Test
    void setRetryDelay_ValidValue_SetsValue() {
        PoolConfig config = new PoolConfig();
        config.setRetryDelay(2000);
        assertEquals(2000, config.getRetryDelay());
    }

    @Test
    void setRetryDelay_NegativeValue_ThrowsException() {
        PoolConfig config = new PoolConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setRetryDelay(-1));
    }

    @Test
    void builderPattern_ChainingMethods_SetsAllValues() {
        PoolConfig config = new PoolConfig()
            .setInitialSize(3)
            .setMaxSize(15)
            .setKeepAliveInterval(20000)
            .setConnectionTimeout(1800000)
            .setAcquireTimeout(10000)
            .setMaxRetries(5)
            .setRetryDelay(2000);

        assertEquals(3, config.getInitialSize());
        assertEquals(15, config.getMaxSize());
        assertEquals(20000, config.getKeepAliveInterval());
        assertEquals(1800000, config.getConnectionTimeout());
        assertEquals(10000, config.getAcquireTimeout());
        assertEquals(5, config.getMaxRetries());
        assertEquals(2000, config.getRetryDelay());
    }
} 