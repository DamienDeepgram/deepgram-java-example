package ai.deepgram.sdk.websocket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AudioStreamOptionsTest {

    @Test
    void constructor_CreatesEmptyOptions() {
        AudioStreamOptions options = new AudioStreamOptions();
        assertEquals("", options.toQueryString());
    }

    @Test
    void setEncoding_ValidInput_SetsEncoding() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16");
        assertEquals("?encoding=linear16", options.toQueryString());
    }

    @Test
    void setSampleRate_ValidInput_SetsSampleRate() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setSampleRate(16000);
        assertEquals("?sample_rate=16000", options.toQueryString());
    }

    @Test
    void setSampleRate_InvalidInput_ThrowsException() {
        AudioStreamOptions options = new AudioStreamOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setSampleRate(0));
        assertThrows(IllegalArgumentException.class, () -> options.setSampleRate(-1));
    }

    @Test
    void setChannels_ValidInput_SetsChannels() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setChannels(2);
        assertEquals("?channels=2", options.toQueryString());
    }

    @Test
    void setChannels_InvalidInput_ThrowsException() {
        AudioStreamOptions options = new AudioStreamOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setChannels(0));
        assertThrows(IllegalArgumentException.class, () -> options.setChannels(-1));
    }

    @Test
    void setLanguage_ValidInput_SetsLanguage() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setLanguage("en-US");
        assertEquals("?language=en-US", options.toQueryString());
    }

    @Test
    void setModel_ValidInput_SetsModel() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setModel("nova-2");
        assertEquals("?model=nova-2", options.toQueryString());
    }

    @Test
    void setPunctuate_ValidInput_SetsPunctuate() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setPunctuate(true);
        assertEquals("?punctuate=true", options.toQueryString());
    }

    @Test
    void setInterimResults_ValidInput_SetsInterimResults() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setInterimResults(true);
        assertEquals("?interim_results=true", options.toQueryString());
    }

    @Test
    void setDiarize_ValidInput_SetsDiarize() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setDiarize(true);
        assertEquals("?diarize=true", options.toQueryString());
    }

    @Test
    void setTier_ValidInput_SetsTier() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setTier("enhanced");
        assertEquals("?tier=enhanced", options.toQueryString());
    }

    @Test
    void setVersion_ValidInput_SetsVersion() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setVersion("latest");
        assertEquals("?version=latest", options.toQueryString());
    }

    @Test
    void toQueryString_MultipleOptions_CreatesValidQueryString() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setLanguage("en-US")
            .setModel("nova-2")
            .setPunctuate(true)
            .setInterimResults(true);

        String queryString = options.toQueryString();
        assertTrue(queryString.startsWith("?"));
        assertTrue(queryString.contains("encoding=linear16"));
        assertTrue(queryString.contains("sample_rate=16000"));
        assertTrue(queryString.contains("language=en-US"));
        assertTrue(queryString.contains("model=nova-2"));
        assertTrue(queryString.contains("punctuate=true"));
        assertTrue(queryString.contains("interim_results=true"));
        assertEquals(6, queryString.split("&").length);
    }

    @Test
    void equals_SameContent_ReturnsTrue() {
        AudioStreamOptions options1 = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000);

        AudioStreamOptions options2 = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000);

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());
    }

    @Test
    void equals_DifferentContent_ReturnsFalse() {
        AudioStreamOptions options1 = new AudioStreamOptions()
            .setEncoding("linear16");

        AudioStreamOptions options2 = new AudioStreamOptions()
            .setEncoding("opus");

        assertNotEquals(options1, options2);
    }

    @Test
    void toString_ContainsAllFields() {
        AudioStreamOptions options = new AudioStreamOptions()
            .setEncoding("linear16")
            .setSampleRate(16000)
            .setLanguage("en-US");

        String toString = options.toString();
        assertTrue(toString.contains("linear16"));
        assertTrue(toString.contains("16000"));
        assertTrue(toString.contains("en-US"));
    }
} 