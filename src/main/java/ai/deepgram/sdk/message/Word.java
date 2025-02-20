package ai.deepgram.sdk.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Word {
    private final String word;
    private final double start;
    private final double end;
    private final double confidence;
    private final String punctuatedWord;

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
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }

        this.word = word;
        this.start = start;
        this.end = end;
        this.confidence = confidence;
        this.punctuatedWord = punctuatedWord;
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

    @Override
    public String toString() {
        return String.format("Word{word='%s', start=%.2f, end=%.2f, confidence=%.2f, punctuatedWord='%s'}",
            word, start, end, confidence, punctuatedWord);
    }
} 