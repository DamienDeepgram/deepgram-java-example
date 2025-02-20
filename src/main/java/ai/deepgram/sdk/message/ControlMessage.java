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
        START,
        STOP,
        ERROR,
        KEEPALIVE
    }

    private final ControlType controlType;
    private final String message;
    private final Integer code;
    private final String details;

    /**
     * Creates a new control message with all parameters.
     *
     * @param controlType the type of control message
     * @param message optional message text
     * @param code optional status code
     * @param details optional additional details
     * @throws IllegalArgumentException if controlType is null or if message is required but null/empty
     */
    @JsonCreator
    public ControlMessage(
        @JsonProperty("controlType") ControlType controlType,
        @JsonProperty("message") String message,
        @JsonProperty("code") Integer code,
        @JsonProperty("details") String details
    ) {
        if (controlType == null) {
            throw new IllegalArgumentException("controlType cannot be null");
        }
        if (controlType == ControlType.ERROR && (message == null || message.trim().isEmpty())) {
            throw new IllegalArgumentException("message is required for ERROR control type");
        }

        this.controlType = controlType;
        this.message = message;
        this.code = code;
        this.details = details;
    }

    /**
     * Creates a new control message with only the control type.
     *
     * @param controlType the type of control message
     * @throws IllegalArgumentException if controlType is null
     */
    public ControlMessage(ControlType controlType) {
        this(controlType, null, null, null);
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
    public ControlType getControlType() {
        return controlType;
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
    public MessageType getType() {
        return MessageType.CONTROL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlMessage that = (ControlMessage) o;
        return controlType == that.controlType &&
               Objects.equals(message, that.message) &&
               Objects.equals(code, that.code) &&
               Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controlType, message, code, details);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ControlMessage{controlType=")
            .append(controlType);
        
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