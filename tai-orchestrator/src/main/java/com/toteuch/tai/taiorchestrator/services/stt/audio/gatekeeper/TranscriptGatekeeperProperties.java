package com.toteuch.tai.taiorchestrator.services.stt.audio.gatekeeper;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "tai.stt.gatekeeper")
public class TranscriptGatekeeperProperties {

    private long rejectAudioDurationMs = 250;
    private long suspiciousAudioDurationMs = 500;
    private double rejectAverageEnergyThreshold = 150.0;
    private double suspiciousLanguageProbabilityThreshold = 0.45;
    private int rejectSuspicionScore = 2;
    private List<String> allowedLanguages = List.of("en", "fr");

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

    public List<String> getAllowedLanguages() {
        return allowedLanguages;
    }

    public void setAllowedLanguages(List<String> allowedLanguages) {
        this.allowedLanguages = allowedLanguages;
    }
}
