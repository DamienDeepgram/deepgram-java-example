package ai.deepgram.sdk.message;

/**
 * Base interface for all Deepgram WebSocket messages.
 * This provides a common type for all messages that can be sent or received
 * through the Deepgram WebSocket connection.
 */
public interface DeepgramMessage {
    /**
     * Gets the type of the message.
     * @return The message type as an enum value
     */
    MessageType getType();

    /**
     * Enum defining the different types of messages that can be exchanged
     * with the Deepgram WebSocket API.
     */
    enum MessageType {
        AUDIO,          // Audio data being sent to Deepgram
        TRANSCRIPT,     // Transcription results from Deepgram
        CONTROL,        // Control messages (start/stop/error)
        METADATA        // Metadata about the connection or stream
    }
} 