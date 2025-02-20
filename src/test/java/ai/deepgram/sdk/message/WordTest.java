package ai.deepgram.sdk.message;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WordTest {
    private static final String TEST_WORD = "test";
    private static final double TEST_START = 1.0;
    private static final double TEST_END = 2.0;
    private static final double TEST_CONFIDENCE = 0.9;
    private static final String TEST_PUNCTUATED = "test.";

    @Test
    void constructor_ValidInput_CreatesInstance() {
        Word word = new Word(TEST_WORD, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED);
        assertEquals(TEST_WORD, word.getWord());
        assertEquals(TEST_START, word.getStart());
        assertEquals(TEST_END, word.getEnd());
        assertEquals(TEST_CONFIDENCE, word.getConfidence());
        assertEquals(TEST_PUNCTUATED, word.getPunctuatedWord());
    }

    @Test
    void constructor_NegativeOneEnd_CreatesInstance() {
        Word word = new Word(TEST_WORD, TEST_START, -1, TEST_CONFIDENCE, TEST_PUNCTUATED);
        assertEquals(TEST_WORD, word.getWord());
        assertEquals(TEST_START, word.getStart());
        assertEquals(-1, word.getEnd());
        assertEquals(TEST_CONFIDENCE, word.getConfidence());
        assertEquals(TEST_PUNCTUATED, word.getPunctuatedWord());
    }

    @Test
    void constructor_NullWord_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Word(null, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED));
    }

    @Test
    void constructor_EmptyWord_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Word("", TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED));
    }

    @Test
    void constructor_NegativeStart_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Word(TEST_WORD, -1.0, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED));
    }

    @Test
    void constructor_EndBeforeStart_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Word(TEST_WORD, TEST_START, 0.5, TEST_CONFIDENCE, TEST_PUNCTUATED));
    }

    @Test
    void constructor_NegativeConfidence_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Word(TEST_WORD, TEST_START, TEST_END, -0.1, TEST_PUNCTUATED));
    }

    @Test
    void constructor_ConfidenceGreaterThanOne_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Word(TEST_WORD, TEST_START, TEST_END, 1.1, TEST_PUNCTUATED));
    }

    @Test
    void equals_SameContent_ReturnsTrue() {
        Word word1 = new Word(TEST_WORD, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED);
        Word word2 = new Word(TEST_WORD, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED);
        assertEquals(word1, word2);
        assertEquals(word1.hashCode(), word2.hashCode());
    }

    @Test
    void equals_DifferentContent_ReturnsFalse() {
        Word word1 = new Word(TEST_WORD, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED);
        Word word2 = new Word("different", TEST_START, TEST_END, TEST_CONFIDENCE, "different.");
        assertNotEquals(word1, word2);
    }

    @Test
    void toString_ValidWord_ReturnsFormattedString() {
        Word word = new Word(TEST_WORD, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED);
        String expected = String.format("Word{word='%s', start=%.2f, end=%.2f, confidence=%.2f, punctuatedWord='%s'}",
            TEST_WORD, TEST_START, TEST_END, TEST_CONFIDENCE, TEST_PUNCTUATED);
        assertEquals(expected, word.toString());
    }
} 