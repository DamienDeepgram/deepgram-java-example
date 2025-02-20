package ai.deepgram.sdk.message;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AudioMessageTest {

    @Test
    void constructor_ValidInput_CreatesMessage() {
        byte[] testData = new byte[]{1, 2, 3, 4};
        String encoding = "linear16";
        int sampleRate = 16000;

        AudioMessage message = new AudioMessage(testData, encoding, sampleRate);

        assertArrayEquals(testData, message.getAudioData());
        assertEquals(encoding, message.getEncoding());
        assertEquals(sampleRate, message.getSampleRate());
        assertEquals(DeepgramMessage.MessageType.AUDIO, message.getType());
    }

    @Test
    void constructor_NullAudioData_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioMessage(null, "linear16", 16000));
    }

    @Test
    void constructor_EmptyAudioData_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioMessage(new byte[]{}, "linear16", 16000));
    }

    @Test
    void constructor_NullEncoding_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioMessage(new byte[]{1, 2, 3}, null, 16000));
    }

    @Test
    void constructor_EmptyEncoding_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioMessage(new byte[]{1, 2, 3}, "", 16000));
    }

    @Test
    void constructor_InvalidSampleRate_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioMessage(new byte[]{1, 2, 3}, "linear16", 0));
        assertThrows(IllegalArgumentException.class,
            () -> new AudioMessage(new byte[]{1, 2, 3}, "linear16", -1));
    }

    @Test
    void getAudioData_ReturnsCopy() {
        byte[] originalData = new byte[]{1, 2, 3, 4};
        AudioMessage message = new AudioMessage(originalData, "linear16", 16000);
        
        byte[] returnedData = message.getAudioData();
        returnedData[0] = 99; // Modify the returned array
        
        // Verify the original data in the message is unchanged
        assertNotEquals(returnedData[0], message.getAudioData()[0]);
    }
} 