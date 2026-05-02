package com.toteuch.tai.stt.listener.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.stt")
public class SttListenerProperties {
    private Capture capture = new Capture();
    private Gatekeeper gatekeeper = new Gatekeeper();
    private Whisper whisper = new Whisper();
    private Orchestrator orchestrator = new Orchestrator();
    private Listener listener = new Listener();

    public Capture getCapture() {
        return capture;
    }

    public void setCapture(Capture capture) {
        this.capture = capture;
    }

    public Gatekeeper getGatekeeper() {
        return gatekeeper;
    }

    public void setGatekeeper(Gatekeeper gatekeeper) {
        this.gatekeeper = gatekeeper;
    }

    public Whisper getWhisper() {
        return whisper;
    }

    public void setWhisper(Whisper whisper) {
        this.whisper = whisper;
    }

    public Orchestrator getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static class Capture {
        private String outputDir = "./input";
        private float sampleRate = 16000;
        private int sampleSizeBits = 16;
        private int channels = 1;
        private boolean signed = true;
        private boolean bigEndian = false;
        private int bufferSize = 4096;
        private double silenceThreshold = 40;
        private long silenceDurationMs = 1200;
        private long minRecordingMs = 800;
        private long maxRecordingMs = 15000;
        private long noSpeechTimeoutMs = 3000;

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public float getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(float sampleRate) {
            this.sampleRate = sampleRate;
        }

        public int getSampleSizeBits() {
            return sampleSizeBits;
        }

        public void setSampleSizeBits(int sampleSizeBits) {
            this.sampleSizeBits = sampleSizeBits;
        }

        public int getChannels() {
            return channels;
        }

        public void setChannels(int channels) {
            this.channels = channels;
        }

        public boolean isSigned() {
            return signed;
        }

        public void setSigned(boolean signed) {
            this.signed = signed;
        }

        public boolean isBigEndian() {
            return bigEndian;
        }

        public void setBigEndian(boolean bigEndian) {
            this.bigEndian = bigEndian;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public double getSilenceThreshold() {
            return silenceThreshold;
        }

        public void setSilenceThreshold(double silenceThreshold) {
            this.silenceThreshold = silenceThreshold;
        }

        public long getSilenceDurationMs() {
            return silenceDurationMs;
        }

        public void setSilenceDurationMs(long silenceDurationMs) {
            this.silenceDurationMs = silenceDurationMs;
        }

        public long getMinRecordingMs() {
            return minRecordingMs;
        }

        public void setMinRecordingMs(long minRecordingMs) {
            this.minRecordingMs = minRecordingMs;
        }

        public long getMaxRecordingMs() {
            return maxRecordingMs;
        }

        public void setMaxRecordingMs(long maxRecordingMs) {
            this.maxRecordingMs = maxRecordingMs;
        }

        public long getNoSpeechTimeoutMs() {
            return noSpeechTimeoutMs;
        }

        public void setNoSpeechTimeoutMs(long noSpeechTimeoutMs) {
            this.noSpeechTimeoutMs = noSpeechTimeoutMs;
        }
    }

    public static class Gatekeeper {
        private List<String> allowedLanguages = new ArrayList<>(List.of("en", "fr"));
        private long rejectAudioDurationMs = 250;
        private long suspiciousAudioDurationMs = 500;
        private double rejectAverageEnergyThreshold = 15;
        private double suspiciousLanguageProbabilityThreshold = 0.45;
        private int rejectSuspicionScore = 2;
        private double minVoicedRatio = 0.10;

        public List<String> getAllowedLanguages() {
            return allowedLanguages;
        }

        public void setAllowedLanguages(List<String> allowedLanguages) {
            this.allowedLanguages = allowedLanguages;
        }

        public long getRejectAudioDurationMs() {
            return rejectAudioDurationMs;
        }

        public void setRejectAudioDurationMs(long rejectAudioDurationMs) {
            this.rejectAudioDurationMs = rejectAudioDurationMs;
        }

        public long getSuspiciousAudioDurationMs() {
            return suspiciousAudioDurationMs;
        }

        public void setSuspiciousAudioDurationMs(long suspiciousAudioDurationMs) {
            this.suspiciousAudioDurationMs = suspiciousAudioDurationMs;
        }

        public double getRejectAverageEnergyThreshold() {
            return rejectAverageEnergyThreshold;
        }

        public void setRejectAverageEnergyThreshold(double rejectAverageEnergyThreshold) {
            this.rejectAverageEnergyThreshold = rejectAverageEnergyThreshold;
        }

        public double getSuspiciousLanguageProbabilityThreshold() {
            return suspiciousLanguageProbabilityThreshold;
        }

        public void setSuspiciousLanguageProbabilityThreshold(
                double suspiciousLanguageProbabilityThreshold) {
            this.suspiciousLanguageProbabilityThreshold = suspiciousLanguageProbabilityThreshold;
        }

        public int getRejectSuspicionScore() {
            return rejectSuspicionScore;
        }

        public void setRejectSuspicionScore(int rejectSuspicionScore) {
            this.rejectSuspicionScore = rejectSuspicionScore;
        }

        public double getMinVoicedRatio() {
            return minVoicedRatio;
        }

        public void setMinVoicedRatio(double minVoicedRatio) {
            this.minVoicedRatio = minVoicedRatio;
        }
    }

    public static class Whisper {
        private String baseUrl = "http://localhost:8095";
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 120000;
        private String transcribeUploadPath = "/whisper/transcribe-upload";
        private String transcribeRawPath = "/whisper/transcribe-raw";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public String getTranscribeUploadPath() {
            return transcribeUploadPath;
        }

        public void setTranscribeUploadPath(String transcribeUploadPath) {
            this.transcribeUploadPath = transcribeUploadPath;
        }

        public String getTranscribeRawPath() {
            return transcribeRawPath;
        }

        public void setTranscribeRawPath(String transcribeRawPath) {
            this.transcribeRawPath = transcribeRawPath;
        }
    }

    public static class Orchestrator {
        private String baseUrl = "http://localhost:8080";
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 10000;
        private Callbacks callbacks = new Callbacks();

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public Callbacks getCallbacks() {
            return callbacks;
        }

        public void setCallbacks(Callbacks callbacks) {
            this.callbacks = callbacks;
        }

        public static class Callbacks {
            private String transcriptAcceptedPath = "/events/stt/transcript-accepted";
            private String transcriptUnintelligiblePath = "/events/stt/transcript-unintelligible";
            private String transcriptNoisePath = "/events/stt/transcript-noise";
            private String speechStartedPath = "/events/stt/speech-started";

            public String getTranscriptAcceptedPath() {
                return transcriptAcceptedPath;
            }

            public void setTranscriptAcceptedPath(String transcriptAcceptedPath) {
                this.transcriptAcceptedPath = transcriptAcceptedPath;
            }

            public String getTranscriptUnintelligiblePath() {
                return transcriptUnintelligiblePath;
            }

            public void setTranscriptUnintelligiblePath(String transcriptUnintelligiblePath) {
                this.transcriptUnintelligiblePath = transcriptUnintelligiblePath;
            }

            public String getTranscriptNoisePath() {
                return transcriptNoisePath;
            }

            public void setTranscriptNoisePath(String transcriptNoisePath) {
                this.transcriptNoisePath = transcriptNoisePath;
            }

            public String getSpeechStartedPath() {
                return speechStartedPath;
            }

            public void setSpeechStartedPath(String speechStartedPath) {
                this.speechStartedPath = speechStartedPath;
            }
        }
    }

    public static class Listener {
        private boolean autoStart = false;
        private boolean continueOnError = true;
        private boolean deleteAudioAfterProcessing = true;
        private boolean publishFinalCallbacks = true;
        private boolean publishSpeechStartedCallbacks = true;

        public boolean isAutoStart() {
            return autoStart;
        }

        public void setAutoStart(boolean autoStart) {
            this.autoStart = autoStart;
        }

        public boolean isContinueOnError() {
            return continueOnError;
        }

        public void setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
        }

        public boolean isDeleteAudioAfterProcessing() {
            return deleteAudioAfterProcessing;
        }

        public void setDeleteAudioAfterProcessing(boolean deleteAudioAfterProcessing) {
            this.deleteAudioAfterProcessing = deleteAudioAfterProcessing;
        }

        public boolean isPublishFinalCallbacks() {
            return publishFinalCallbacks;
        }

        public void setPublishFinalCallbacks(boolean publishFinalCallbacks) {
            this.publishFinalCallbacks = publishFinalCallbacks;
        }

        public boolean isPublishSpeechStartedCallbacks() {
            return publishSpeechStartedCallbacks;
        }

        public void setPublishSpeechStartedCallbacks(boolean publishSpeechStartedCallbacks) {
            this.publishSpeechStartedCallbacks = publishSpeechStartedCallbacks;
        }
    }
}
