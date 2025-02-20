package ai.deepgram.sdk.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration options for the audio stream connection to Deepgram.
 * Uses the builder pattern for fluent configuration.
 */
public class AudioStreamOptions {
    private String encoding;
    private Integer sampleRate;
    private String language;
    private String model;
    private Boolean punctuate;
    private Boolean interimResults;
    private Boolean diarize;
    private String tier;
    private Integer channels;
    private String version;

    /**
     * Creates a new AudioStreamOptions instance with default values.
     */
    public AudioStreamOptions() {
        // Default values can be set here if needed
    }

    /**
     * Sets the audio encoding format.
     *
     * @param encoding The encoding format (e.g., "linear16", "opus", "mulaw")
     * @return this instance for chaining
     */
    public AudioStreamOptions setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Sets the audio sample rate.
     *
     * @param sampleRate The sample rate in Hz
     * @return this instance for chaining
     */
    public AudioStreamOptions setSampleRate(int sampleRate) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be positive");
        }
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * Sets the language for transcription.
     *
     * @param language The language code (e.g., "en-US")
     * @return this instance for chaining
     */
    public AudioStreamOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Sets the model to use for transcription.
     *
     * @param model The model name (e.g., "nova-2")
     * @return this instance for chaining
     */
    public AudioStreamOptions setModel(String model) {
        this.model = model;
        return this;
    }

    /**
     * Sets whether to add punctuation to the transcript.
     *
     * @param punctuate Whether to add punctuation
     * @return this instance for chaining
     */
    public AudioStreamOptions setPunctuate(boolean punctuate) {
        this.punctuate = punctuate;
        return this;
    }

    /**
     * Sets whether to receive interim (partial) results.
     *
     * @param interimResults Whether to receive interim results
     * @return this instance for chaining
     */
    public AudioStreamOptions setInterimResults(boolean interimResults) {
        this.interimResults = interimResults;
        return this;
    }

    /**
     * Sets whether to perform speaker diarization.
     *
     * @param diarize Whether to perform diarization
     * @return this instance for chaining
     */
    public AudioStreamOptions setDiarize(boolean diarize) {
        this.diarize = diarize;
        return this;
    }

    /**
     * Sets the API tier to use.
     *
     * @param tier The API tier
     * @return this instance for chaining
     */
    public AudioStreamOptions setTier(String tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Sets the number of audio channels.
     *
     * @param channels The number of channels
     * @return this instance for chaining
     */
    public AudioStreamOptions setChannels(int channels) {
        if (channels <= 0) {
            throw new IllegalArgumentException("Number of channels must be positive");
        }
        this.channels = channels;
        return this;
    }

    /**
     * Sets the API version to use.
     *
     * @param version The API version
     * @return this instance for chaining
     */
    public AudioStreamOptions setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Converts the options to a query string for the WebSocket URL.
     *
     * @return The query string representation of the options
     */
    public String toQueryString() {
        Map<String, String> params = new HashMap<>();

        if (encoding != null) params.put("encoding", encoding);
        if (sampleRate != null) params.put("sample_rate", sampleRate.toString());
        if (language != null) params.put("language", language);
        if (model != null) params.put("model", model);
        if (punctuate != null) params.put("punctuate", punctuate.toString());
        if (interimResults != null) params.put("interim_results", interimResults.toString());
        if (diarize != null) params.put("diarize", diarize.toString());
        if (tier != null) params.put("tier", tier);
        if (channels != null) params.put("channels", channels.toString());
        if (version != null) params.put("version", version);

        if (params.isEmpty()) {
            return "";
        }

        StringBuilder query = new StringBuilder("?");
        params.forEach((key, value) -> {
            if (query.length() > 1) {
                query.append("&");
            }
            query.append(key).append("=").append(value);
        });

        return query.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioStreamOptions that = (AudioStreamOptions) o;
        return Objects.equals(encoding, that.encoding) &&
               Objects.equals(sampleRate, that.sampleRate) &&
               Objects.equals(language, that.language) &&
               Objects.equals(model, that.model) &&
               Objects.equals(punctuate, that.punctuate) &&
               Objects.equals(interimResults, that.interimResults) &&
               Objects.equals(diarize, that.diarize) &&
               Objects.equals(tier, that.tier) &&
               Objects.equals(channels, that.channels) &&
               Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encoding, sampleRate, language, model, punctuate,
                          interimResults, diarize, tier, channels, version);
    }

    @Override
    public String toString() {
        return "AudioStreamOptions{" +
               "encoding='" + encoding + '\'' +
               ", sampleRate=" + sampleRate +
               ", language='" + language + '\'' +
               ", model='" + model + '\'' +
               ", punctuate=" + punctuate +
               ", interimResults=" + interimResults +
               ", diarize=" + diarize +
               ", tier='" + tier + '\'' +
               ", channels=" + channels +
               ", version='" + version + '\'' +
               '}';
    }

    public String appendToUrl(String baseUrl) {
        StringBuilder url = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) {
            url.append("?");
        } else if (!baseUrl.endsWith("&")) {
            url.append("&");
        }

        if (encoding != null) {
            url.append("encoding=").append(encoding).append("&");
        }
        if (sampleRate != null && sampleRate > 0) {
            url.append("sample_rate=").append(sampleRate).append("&");
        }
        if (channels != null && channels > 0) {
            url.append("channels=").append(channels).append("&");
        }
        if (model != null) {
            url.append("model=").append(model).append("&");
        }
        if (language != null) {
            url.append("language=").append(language).append("&");
        }
        if (punctuate != null) {
            url.append("punctuate=").append(punctuate).append("&");
        }
        if (interimResults != null) {
            url.append("interim_results=").append(interimResults).append("&");
        }
        if (diarize != null) {
            url.append("diarize=").append(diarize).append("&");
        }
        if (tier != null) {
            url.append("tier=").append(tier).append("&");
        }
        if (version != null) {
            url.append("version=").append(version).append("&");
        }

        String result = url.toString();
        return result.endsWith("&") ? result.substring(0, result.length() - 1) : result;
    }

    public String getEncoding() {
        return encoding;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public String getLanguage() {
        return language;
    }

    public String getModel() {
        return model;
    }

    public Boolean getPunctuate() {
        return punctuate;
    }

    public Boolean getInterimResults() {
        return interimResults;
    }

    public Boolean getDiarize() {
        return diarize;
    }

    public String getTier() {
        return tier;
    }

    public Integer getChannels() {
        return channels;
    }

    public String getVersion() {
        return version;
    }
} 