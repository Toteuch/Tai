package com.toteuch.tai.stt.listener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tai.stt")
public class SttListenerProperties {
    private Capture capture = new Capture();
    private Gatekeeper gatekeeper = new Gatekeeper();

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

        public void setSuspiciousLanguageProbabilityThreshold(double suspiciousLanguageProbabilityThreshold) {
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
}
