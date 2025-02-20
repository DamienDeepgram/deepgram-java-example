package ai.deepgram.sdk.message;

import java.util.Arrays;

/**
 * Represents an audio message containing raw audio data to be sent to Deepgram.
 * This class handles the binary audio data and any associated metadata.
 */
public class AudioMessage implements DeepgramMessage {
    private final byte[] audioData;
    private final String encoding;
    private final int sampleRate;

    /**
     * Creates a new AudioMessage with the specified audio data and format information.
     *
     * @param audioData The raw audio data bytes
     * @param encoding The audio encoding format (e.g., "linear16", "opus", "mulaw")
     * @param sampleRate The sample rate of the audio in Hz
     */
    public AudioMessage(byte[] audioData, String encoding, int sampleRate) {
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("Audio data cannot be null or empty");
        }
        if (encoding == null || encoding.trim().isEmpty()) {
            throw new IllegalArgumentException("Encoding cannot be null or empty");
        }
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be positive");
        }

        this.audioData = Arrays.copyOf(audioData, audioData.length);
        this.encoding = encoding;
        this.sampleRate = sampleRate;
    }

    /**
     * Gets the raw audio data.
     * @return A copy of the audio data bytes
     */
    public byte[] getAudioData() {
        return Arrays.copyOf(audioData, audioData.length);
    }

    /**
     * Gets the audio encoding format.
     * @return The encoding format string
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Gets the audio sample rate.
     * @return The sample rate in Hz
     */
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public MessageType getType() {
        return MessageType.AUDIO;
    }
} 