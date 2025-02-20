package ai.deepgram.sdk.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents a control message in the WebSocket communication.
 * Control messages are used to manage the WebSocket connection state and handle errors.
 */
public class ControlMessage implements DeepgramMessage {

    /**
     * Enumeration of possible control message types.
     */
    public enum ControlType {
        START("StartStream"),
        STOP("CloseStream"),
        ERROR("Error"),
        KEEPALIVE("KeepAlive");

        private final String value;

        ControlType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final ControlType type;
    private final String message;
    private final Integer code;
    private final String details;

    /**
     * Creates a new control message with all parameters.
     *
     * @param type the type of control message
     * @param message optional message text
     * @param code optional status code
     * @param details optional additional details
     * @throws IllegalArgumentException if type is null or if message is required but null/empty
     */
    @JsonCreator
    public ControlMessage(
        @JsonProperty("type") ControlType type,
        @JsonProperty("message") String message,
        @JsonProperty("code") Integer code,
        @JsonProperty("details") String details
    ) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (type == ControlType.ERROR && (message == null || message.trim().isEmpty())) {
            throw new IllegalArgumentException("message is required for ERROR control type");
        }

        this.type = type;
        this.message = message;
        this.code = code;
        this.details = details;
    }

    /**
     * Creates a new control message with only the control type.
     *
     * @param type the type of control message
     * @throws IllegalArgumentException if type is null
     */
    public ControlMessage(ControlType type) {
        this(type, null, null, null);
    }

    /**
     * Creates an error control message with all parameters.
     *
     * @param message the error message
     * @param code the error code
     * @param details additional error details
     * @return a new error control message
     * @throws IllegalArgumentException if message is null or empty
     */
    public static ControlMessage createError(String message, Integer code, String details) {
        return new ControlMessage(ControlType.ERROR, message, code, details);
    }

    /**
     * Creates an error control message with only a message.
     *
     * @param message the error message
     * @return a new error control message
     * @throws IllegalArgumentException if message is null or empty
     */
    public static ControlMessage createError(String message) {
        return new ControlMessage(ControlType.ERROR, message, null, null);
    }

    /**
     * Creates a start control message.
     *
     * @return a new start control message
     */
    public static ControlMessage createStart() {
        return new ControlMessage(ControlType.START);
    }

    /**
     * Creates a stop control message.
     *
     * @return a new stop control message
     */
    public static ControlMessage createStop() {
        return new ControlMessage(ControlType.STOP);
    }

    /**
     * Creates a keepalive control message.
     *
     * @return a new keepalive control message
     */
    public static ControlMessage createKeepalive() {
        return new ControlMessage(ControlType.KEEPALIVE);
    }

    /**
     * Gets the control type of this message.
     *
     * @return the control type
     */
    @Override
    public MessageType getType() {
        return MessageType.CONTROL;
    }

    /**
     * Gets the control type.
     *
     * @return the control type
     */
    public ControlType getControlType() {
        return type;
    }

    /**
     * Gets the message text.
     *
     * @return the message text, or null if not set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the status code.
     *
     * @return the status code, or null if not set
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Gets the additional details.
     *
     * @return the additional details, or null if not set
     */
    public String getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlMessage that = (ControlMessage) o;
        return type == that.type &&
               Objects.equals(message, that.message) &&
               Objects.equals(code, that.code) &&
               Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message, code, details);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ControlMessage{type=")
            .append(type);
        
        if (message != null) {
            sb.append(", message='").append(message).append('\'');
        }
        if (code != null) {
            sb.append(", code=").append(code);
        }
        if (details != null) {
            sb.append(", details='").append(details).append('\'');
        }
        
        return sb.append('}').toString();
    }
} 