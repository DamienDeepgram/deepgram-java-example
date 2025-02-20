package ai.deepgram.sdk.websocket;

import ai.deepgram.sdk.message.TranscriptMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranscriptResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructor_ValidInput_CreatesResponse() {
        List<TranscriptMessage.Word> words = Arrays.asList(
            new TranscriptMessage.Word("Hello", 0.0, 0.5, 0.9, "Hello"),
            new TranscriptMessage.Word("world", 0.6, 1.0, 0.95, "world")
        );

        TranscriptResponse.Alternative alternative = new TranscriptResponse.Alternative(
            "Hello world", 0.925, words
        );

        TranscriptResponse.Channel channel = new TranscriptResponse.Channel(
            List.of(alternative)
        );

        TranscriptResponse response = new TranscriptResponse();
        response.setType("Results");
        response.setChannelIndex(Arrays.asList(0, 1));
        response.setStart(0.0);
        response.setDuration(1.0);
        response.setFinal(false);
        response.setSpeechFinal(false);
        response.setFromFinalize(false);
        response.setChannel(channel);

        assertNotNull(response.getChannel());
        assertEquals("Results", response.getType());
        assertEquals(Arrays.asList(0, 1), response.getChannelIndex());
        assertEquals(0.0, response.getStart());
        assertEquals(1.0, response.getDuration());
        assertFalse(response.isFinal());
        assertFalse(response.isSpeechFinal());
        assertFalse(response.isFromFinalize());
        assertEquals(1, response.getChannel().getAlternatives().size());

        TranscriptResponse.Alternative firstAlt = response.getChannel().getAlternatives().get(0);
        assertEquals("Hello world", firstAlt.getTranscript());
        assertEquals(0.925, firstAlt.getConfidence());
        assertEquals(2, firstAlt.getWords().size());
    }

    @Test
    void toMessage_ValidResponse_CreatesMessage() {
        List<TranscriptMessage.Word> words = Arrays.asList(
            new TranscriptMessage.Word("Hello", 0.0, 0.5, 0.9, "Hello"),
            new TranscriptMessage.Word("world", 0.6, 1.0, 0.95, "world")
        );

        TranscriptResponse.Alternative alternative = new TranscriptResponse.Alternative(
            "Hello world", 0.925, words
        );

        TranscriptResponse.Channel channel = new TranscriptResponse.Channel(
            List.of(alternative)
        );

        TranscriptResponse response = new TranscriptResponse();
        response.setChannelIndex(Arrays.asList(0));
        response.setStart(0.0);
        response.setDuration(1.0);
        response.setChannel(channel);

        TranscriptMessage message = response.toMessage();

        assertEquals("Hello world", message.getTranscript());
        assertEquals(0.925, message.getConfidence());
        assertEquals("0", message.getChannel());
        assertEquals(0.0, message.getStart());
        assertEquals(1.0, message.getDuration());
        assertEquals(2, message.getWords().length);
    }

    @Test
    void toMessage_NoChannel_ReturnsNull() {
        TranscriptResponse response = new TranscriptResponse();
        assertNull(response.toMessage());
    }

    @Test
    void toMessage_NoAlternatives_ReturnsNull() {
        TranscriptResponse.Channel channel = new TranscriptResponse.Channel(null);
        TranscriptResponse response = new TranscriptResponse();
        response.setChannel(channel);
        assertNull(response.toMessage());
    }

    @Test
    void toMessage_EmptyAlternatives_ReturnsNull() {
        TranscriptResponse.Channel channel = new TranscriptResponse.Channel(List.of());
        TranscriptResponse response = new TranscriptResponse();
        response.setChannel(channel);
        assertNull(response.toMessage());
    }

    @Test
    void jsonSerialization_ValidResponse_SerializesCorrectly() throws Exception {
        List<TranscriptMessage.Word> words = Arrays.asList(
            new TranscriptMessage.Word("Hello", 0.0, 0.5, 0.9, "Hello"),
            new TranscriptMessage.Word("world", 0.6, 1.0, 0.95, "world")
        );

        TranscriptResponse.Alternative alternative = new TranscriptResponse.Alternative(
            "Hello world", 0.925, words
        );

        TranscriptResponse.Channel channel = new TranscriptResponse.Channel(
            List.of(alternative)
        );

        TranscriptResponse response = new TranscriptResponse();
        response.setType("Results");
        response.setChannelIndex(Arrays.asList(0, 1));
        response.setStart(0.0);
        response.setDuration(1.0);
        response.setFinal(false);
        response.setSpeechFinal(false);
        response.setFromFinalize(false);
        response.setChannel(channel);

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"type\":\"Results\""));
        assertTrue(json.contains("\"channel_index\":[0,1]"));
        assertTrue(json.contains("\"start\":0.0"));
        assertTrue(json.contains("\"duration\":1.0"));
        assertTrue(json.contains("\"is_final\":false"));
        assertTrue(json.contains("\"speech_final\":false"));
        assertTrue(json.contains("\"from_finalize\":false"));
        assertTrue(json.contains("\"transcript\":\"Hello world\""));
        assertTrue(json.contains("\"confidence\":0.925"));
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        String json = "{"
            + "\"type\":\"Results\","
            + "\"channel_index\":[0,1],"
            + "\"duration\":1.7899399,"
            + "\"start\":96.25,"
            + "\"is_final\":false,"
            + "\"speech_final\":false,"
            + "\"channel\":{"
            + "    \"alternatives\":[{"
            + "        \"transcript\":\"Hello world\","
            + "        \"confidence\":0.925,"
            + "        \"words\":["
            + "            {\"word\":\"Hello\",\"start\":0.0,\"end\":0.5,\"confidence\":0.9,\"punctuated_word\":\"Hello\"},"
            + "            {\"word\":\"world\",\"start\":0.6,\"end\":1.0,\"confidence\":0.95,\"punctuated_word\":\"world\"}"
            + "        ]"
            + "    }]"
            + "},"
            + "\"metadata\":{"
            + "    \"request_id\":\"7b0cf917-d66b-4c55-a6dd-415e9b63e1ce\","
            + "    \"model_info\":{"
            + "        \"name\":\"2-general-nova\","
            + "        \"version\":\"2024-01-11.36317\","
            + "        \"arch\":\"nova-2\""
            + "    },"
            + "    \"model_uuid\":\"1dbdfb4d-85b2-4659-9831-16b3c76229aa\""
            + "},"
            + "\"from_finalize\":false"
            + "}";

        TranscriptResponse response = objectMapper.readValue(json, TranscriptResponse.class);

        assertNotNull(response);
        assertEquals("Results", response.getType());
        assertEquals(Arrays.asList(0, 1), response.getChannelIndex());
        assertEquals(1.7899399, response.getDuration());
        assertEquals(96.25, response.getStart());
        assertFalse(response.isFinal());
        assertFalse(response.isSpeechFinal());
        assertFalse(response.isFromFinalize());

        assertNotNull(response.getChannel());
        List<TranscriptResponse.Alternative> alternatives = response.getChannel().getAlternatives();
        assertEquals(1, alternatives.size());
        assertEquals("Hello world", alternatives.get(0).getTranscript());
        assertEquals(0.925, alternatives.get(0).getConfidence());
        assertEquals(2, alternatives.get(0).getWords().size());

        assertNotNull(response.getMetadata());
        assertEquals("7b0cf917-d66b-4c55-a6dd-415e9b63e1ce", response.getMetadata().getRequestId());
        assertEquals("2-general-nova", response.getMetadata().getModelInfo().getName());
        assertEquals("2024-01-11.36317", response.getMetadata().getModelInfo().getVersion());
        assertEquals("nova-2", response.getMetadata().getModelInfo().getArch());
        assertEquals("1dbdfb4d-85b2-4659-9831-16b3c76229aa", response.getMetadata().getModelUuid());
    }
}