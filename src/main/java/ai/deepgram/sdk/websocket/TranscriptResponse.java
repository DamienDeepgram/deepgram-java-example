package ai.deepgram.sdk.websocket;

import ai.deepgram.sdk.message.TranscriptMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscriptResponse {
    private String type;
    @JsonProperty("channel_index")
    private List<Integer> channelIndex;
    private double duration;
    private double start;
    @JsonProperty("is_final")
    private boolean isFinal;
    @JsonProperty("speech_final")
    private boolean speechFinal;
    private Channel channel;
    private Metadata metadata;
    @JsonProperty("from_finalize")
    private boolean fromFinalize;

    public TranscriptResponse() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(List<Integer> channelIndex) {
        this.channelIndex = channelIndex;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isSpeechFinal() {
        return speechFinal;
    }

    public void setSpeechFinal(boolean speechFinal) {
        this.speechFinal = speechFinal;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public boolean isFromFinalize() {
        return fromFinalize;
    }

    public void setFromFinalize(boolean fromFinalize) {
        this.fromFinalize = fromFinalize;
    }

    public TranscriptMessage toMessage() {
        if (channel == null || channel.alternatives == null || channel.alternatives.isEmpty()) {
            return null;
        }

        Alternative alternative = channel.alternatives.get(0);
        String channelId = channelIndex != null && !channelIndex.isEmpty() ? 
            String.valueOf(channelIndex.get(0)) : "default";

        return new TranscriptMessage(
            alternative.transcript,
            alternative.confidence,
            channelId,
            start,
            duration,
            alternative.words != null ? alternative.words.toArray(new TranscriptMessage.Word[0]) : null,
            isFinal
        );
    }

    public static class Channel {
        private List<Alternative> alternatives = new ArrayList<>();

        public Channel() {}

        public Channel(List<Alternative> alternatives) {
            this.alternatives = alternatives != null ? alternatives : new ArrayList<>();
        }

        public List<Alternative> getAlternatives() {
            return alternatives;
        }

        public void setAlternatives(List<Alternative> alternatives) {
            this.alternatives = alternatives != null ? alternatives : new ArrayList<>();
        }
    }

    public static class Alternative {
        private String transcript;
        private double confidence;
        private List<TranscriptMessage.Word> words = new ArrayList<>();

        public Alternative() {}

        public Alternative(String transcript, double confidence, List<TranscriptMessage.Word> words) {
            this.transcript = transcript;
            this.confidence = confidence;
            this.words = words != null ? words : new ArrayList<>();
        }

        public String getTranscript() {
            return transcript;
        }

        public double getConfidence() {
            return confidence;
        }

        public List<TranscriptMessage.Word> getWords() {
            return words;
        }

        public void setTranscript(String transcript) {
            this.transcript = transcript;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public void setWords(List<TranscriptMessage.Word> words) {
            this.words = words != null ? words : new ArrayList<>();
        }
    }

    public static class Metadata {
        @JsonProperty("request_id")
        private String requestId;
        @JsonProperty("model_info")
        private ModelInfo modelInfo;
        @JsonProperty("model_uuid")
        private String modelUuid;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public ModelInfo getModelInfo() {
            return modelInfo;
        }

        public void setModelInfo(ModelInfo modelInfo) {
            this.modelInfo = modelInfo;
        }

        public String getModelUuid() {
            return modelUuid;
        }

        public void setModelUuid(String modelUuid) {
            this.modelUuid = modelUuid;
        }
    }

    public static class ModelInfo {
        private String name;
        private String version;
        private String arch;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getArch() {
            return arch;
        }

        public void setArch(String arch) {
            this.arch = arch;
        }
    }
} 