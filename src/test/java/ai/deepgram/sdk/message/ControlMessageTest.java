package ai.deepgram.sdk.message;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControlMessageTest {

    @Test
    void constructor_ValidInput_CreatesMessage() {
        ControlMessage message = new ControlMessage(
            ControlMessage.ControlType.START,
            "Starting stream",
            200,
            "Additional details"
        );

        assertEquals(ControlMessage.ControlType.START, message.getControlType());
        assertEquals("Starting stream", message.getMessage());
        assertEquals(Integer.valueOf(200), message.getCode());
        assertEquals("Additional details", message.getDetails());
        assertEquals(DeepgramMessage.MessageType.CONTROL, message.getType());
    }

    @Test
    void constructor_NullControlType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ControlMessage(null, "message", 200, "details")
        );
    }

    @Test
    void constructor_ErrorWithoutMessage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ControlMessage(ControlMessage.ControlType.ERROR, null, 500, "details")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new ControlMessage(ControlMessage.ControlType.ERROR, "", 500, "details")
        );
    }

    @Test
    void constructor_SimpleConstructor_CreatesMessage() {
        ControlMessage message = new ControlMessage(ControlMessage.ControlType.START);

        assertEquals(ControlMessage.ControlType.START, message.getControlType());
        assertNull(message.getMessage());
        assertNull(message.getCode());
        assertNull(message.getDetails());
    }

    @Test
    void createError_WithAllParameters_CreatesErrorMessage() {
        ControlMessage message = ControlMessage.createError(
            "Error occurred",
            500,
            "Detailed error information"
        );

        assertEquals(ControlMessage.ControlType.ERROR, message.getControlType());
        assertEquals("Error occurred", message.getMessage());
        assertEquals(Integer.valueOf(500), message.getCode());
        assertEquals("Detailed error information", message.getDetails());
    }

    @Test
    void createError_WithMessageOnly_CreatesErrorMessage() {
        ControlMessage message = ControlMessage.createError("Error occurred");

        assertEquals(ControlMessage.ControlType.ERROR, message.getControlType());
        assertEquals("Error occurred", message.getMessage());
        assertNull(message.getCode());
        assertNull(message.getDetails());
    }

    @Test
    void createStart_CreatesStartMessage() {
        ControlMessage message = ControlMessage.createStart();

        assertEquals(ControlMessage.ControlType.START, message.getControlType());
        assertNull(message.getMessage());
        assertNull(message.getCode());
        assertNull(message.getDetails());
    }

    @Test
    void createStop_CreatesStopMessage() {
        ControlMessage message = ControlMessage.createStop();

        assertEquals(ControlMessage.ControlType.STOP, message.getControlType());
        assertNull(message.getMessage());
        assertNull(message.getCode());
        assertNull(message.getDetails());
    }

    @Test
    void createKeepalive_CreatesKeepaliveMessage() {
        ControlMessage message = ControlMessage.createKeepalive();

        assertEquals(ControlMessage.ControlType.KEEPALIVE, message.getControlType());
        assertNull(message.getMessage());
        assertNull(message.getCode());
        assertNull(message.getDetails());
    }

    @Test
    void equals_SameContent_ReturnsTrue() {
        ControlMessage message1 = new ControlMessage(
            ControlMessage.ControlType.START,
            "message",
            200,
            "details"
        );

        ControlMessage message2 = new ControlMessage(
            ControlMessage.ControlType.START,
            "message",
            200,
            "details"
        );

        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    void equals_DifferentContent_ReturnsFalse() {
        ControlMessage message1 = new ControlMessage(
            ControlMessage.ControlType.START,
            "message1",
            200,
            "details"
        );

        ControlMessage message2 = new ControlMessage(
            ControlMessage.ControlType.START,
            "message2",
            200,
            "details"
        );

        assertNotEquals(message1, message2);
    }

    @Test
    void toString_ContainsAllFields() {
        ControlMessage message = new ControlMessage(
            ControlMessage.ControlType.ERROR,
            "Error message",
            500,
            "Error details"
        );

        String toString = message.toString();
        assertTrue(toString.contains("ERROR"));
        assertTrue(toString.contains("Error message"));
        assertTrue(toString.contains("500"));
        assertTrue(toString.contains("Error details"));
    }

    @Test
    void toString_OmitsNullFields() {
        ControlMessage message = new ControlMessage(ControlMessage.ControlType.START);

        String toString = message.toString();
        assertTrue(toString.contains("START"));
        assertFalse(toString.contains("message"));
        assertFalse(toString.contains("code"));
        assertFalse(toString.contains("details"));
    }
} 