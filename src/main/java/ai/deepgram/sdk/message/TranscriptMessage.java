package ai.deepgram.sdk.message;

import java.util.Arrays;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a transcription result message received from Deepgram.
 * This class contains the transcribed text along with metadata such as
 * timing information, confidence scores, and speaker identification.
 */
public class TranscriptMessage {
    private final String transcript;
    private final double confidence;
    private final String channel;
    private final double start;
    private final double duration;
    private final Word[] words;
    private final boolean isFinal;

    /**
     * Creates a new TranscriptMessage with the specified parameters.
     *
     * @param transcript The transcribed text
     * @param confidence The confidence score (0.0 to 1.0)
     * @param channel The audio channel identifier
     * @param start The start time in seconds
     * @param duration The duration in seconds
     * @param words Array of individual words with their timing information
     * @param isFinal Whether this is a final transcript or an interim result
     */
    @JsonCreator
    public TranscriptMessage(
            @JsonProperty("transcript") String transcript,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("channel") String channel,
            @JsonProperty("start") double start,
            @JsonProperty("duration") double duration,
            @JsonProperty("words") Word[] words,
            @JsonProperty("is_final") boolean isFinal) {
        if (transcript == null || transcript.trim().isEmpty()) {
            throw new IllegalArgumentException("Transcript cannot be null or empty");
        }
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
        if (channel == null || channel.trim().isEmpty()) {
            throw new IllegalArgumentException("Channel cannot be null or empty");
        }
        if (start < 0) {
            throw new IllegalArgumentException("Start time cannot be negative");
        }
        if (duration < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        this.transcript = transcript;
        this.confidence = confidence;
        this.channel = channel;
        this.start = start;
        this.duration = duration;
        this.words = words != null ? Arrays.copyOf(words, words.length) : new Word[0];
        this.isFinal = isFinal;
    }

    public String getTranscript() {
        return transcript;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getChannel() {
        return channel;
    }

    public double getStart() {
        return start;
    }

    public double getDuration() {
        return duration;
    }

    public Word[] getWords() {
        return Arrays.copyOf(words, words.length);
    }

    /**
     * Gets whether this is a final transcript or an interim result.
     *
     * @return true if this is a final transcript, false if it's an interim result
     */
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptMessage that = (TranscriptMessage) o;
        return Double.compare(that.confidence, confidence) == 0 &&
               Double.compare(that.start, start) == 0 &&
               Double.compare(that.duration, duration) == 0 &&
               Objects.equals(transcript, that.transcript) &&
               Objects.equals(channel, that.channel) &&
               Arrays.equals(words, that.words);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(transcript, confidence, channel, start, duration);
        result = 31 * result + Arrays.hashCode(words);
        return result;
    }

    @Override
    public String toString() {
        return String.format("TranscriptMessage{transcript='%s', confidence=%.2f, channel='%s', start=%.2f, duration=%.2f, words=%s}",
            transcript, confidence, channel, start, duration, Arrays.toString(words));
    }

    /**
     * Represents a single word in the transcript with its timing and confidence information.
     */
    public static class Word {
        private final String word;
        private final double start;
        private final double end;
        private final double confidence;
        private final String punctuatedWord;

        @JsonCreator
        public Word(
            @JsonProperty("word") String word,
            @JsonProperty("start") double start,
            @JsonProperty("end") double end,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("punctuated_word") String punctuatedWord
        ) {
            if (word == null || word.trim().isEmpty()) {
                throw new IllegalArgumentException("Word cannot be null or empty");
            }
            if (start < 0) {
                throw new IllegalArgumentException("Start time cannot be negative");
            }
            if (end != -1 && end < start) {
                throw new IllegalArgumentException("End time cannot be before start time");
            }
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
            }

            this.word = word;
            this.start = start;
            this.end = end;
            this.confidence = confidence;
            this.punctuatedWord = punctuatedWord;
        }

        public Word() {
            this("", 0.0, 0.0, 0.0, "");
        }

        public String getWord() {
            return word;
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getPunctuatedWord() {
            return punctuatedWord;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Word that = (Word) o;
            return Double.compare(that.start, start) == 0 &&
                   Double.compare(that.end, end) == 0 &&
                   Double.compare(that.confidence, confidence) == 0 &&
                   Objects.equals(word, that.word) &&
                   Objects.equals(punctuatedWord, that.punctuatedWord);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, start, end, confidence, punctuatedWord);
        }
    }
}