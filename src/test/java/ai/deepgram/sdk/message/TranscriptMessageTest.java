package ai.deepgram.sdk.message;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ai.deepgram.sdk.message.Word;

public class TranscriptMessageTest {
    private static final String TEST_TRANSCRIPT = "test transcript";
    private static final double TEST_CONFIDENCE = 0.95;
    private static final String TEST_CHANNEL = "test_channel";
    private static final double TEST_START = 1.0;
    private static final double TEST_DURATION = 2.0;
    private static final boolean TEST_IS_FINAL = true;
    private static final TranscriptMessage.Word[] TEST_WORDS = new TranscriptMessage.Word[]{
        new TranscriptMessage.Word("test", 1.0, 1.5, 0.9, "test"),
        new TranscriptMessage.Word("transcript", 1.6, 2.0, 0.95, "transcript")
    };

    @Test
    void constructor_ValidInput_CreatesInstance() {
        TranscriptMessage message = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        assertEquals(TEST_TRANSCRIPT, message.getTranscript());
        assertEquals(TEST_CONFIDENCE, message.getConfidence());
        assertEquals(TEST_CHANNEL, message.getChannel());
        assertEquals(TEST_START, message.getStart());
        assertEquals(TEST_DURATION, message.getDuration());
        assertArrayEquals(TEST_WORDS, message.getWords());
        assertEquals(TEST_IS_FINAL, message.isFinal());
    }

    @Test
    void constructor_NullTranscript_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(null, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_EmptyTranscript_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage("", TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_NegativeConfidence_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(TEST_TRANSCRIPT, -0.1, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_ConfidenceGreaterThanOne_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(TEST_TRANSCRIPT, 1.1, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_NullChannel_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(TEST_TRANSCRIPT, TEST_CONFIDENCE, null, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_EmptyChannel_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(TEST_TRANSCRIPT, TEST_CONFIDENCE, "", TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_NegativeStart_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, -1.0, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_NegativeDuration_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new TranscriptMessage(TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, -1.0, TEST_WORDS, TEST_IS_FINAL)
        );
    }

    @Test
    void constructor_NullWords_CreatesEmptyArray() {
        TranscriptMessage message = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, null, TEST_IS_FINAL
        );
        assertNotNull(message.getWords());
        assertEquals(0, message.getWords().length);
    }

    @Test
    void equals_SameObject_ReturnsTrue() {
        TranscriptMessage message = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        assertEquals(message, message);
    }

    @Test
    void equals_NullObject_ReturnsFalse() {
        TranscriptMessage message = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        assertNotEquals(null, message);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        TranscriptMessage message = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        assertNotEquals("not a message", message);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        TranscriptMessage message1 = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        TranscriptMessage message2 = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        TranscriptMessage message1 = new TranscriptMessage(
            TEST_TRANSCRIPT, TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        TranscriptMessage message2 = new TranscriptMessage(
            "different", TEST_CONFIDENCE, TEST_CHANNEL, TEST_START, TEST_DURATION, TEST_WORDS, TEST_IS_FINAL
        );
        assertNotEquals(message1, message2);
    }
} 