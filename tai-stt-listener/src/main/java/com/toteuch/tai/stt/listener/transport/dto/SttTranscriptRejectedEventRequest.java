package com.toteuch.tai.stt.listener.transport.dto;

public class SttTranscriptRejectedEventRequest extends AbstractTransportEventRequest {
    private String language;
    private Double languageProbability;
    private Long durationMs;
    private Double averageEnergy;
    private String reason;
    private Integer suspicionScore;
    private Long transcriptionDurationMs;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getLanguageProbability() {
        return languageProbability;
    }

    public void setLanguageProbability(Double languageProbability) {
        this.languageProbability = languageProbability;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Double getAverageEnergy() {
        return averageEnergy;
    }

    public void setAverageEnergy(Double averageEnergy) {
        this.averageEnergy = averageEnergy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getSuspicionScore() {
        return suspicionScore;
    }

    public void setSuspicionScore(Integer suspicionScore) {
        this.suspicionScore = suspicionScore;
    }

    public Long getTranscriptionDurationMs() {
        return transcriptionDurationMs;
    }

    public void setTranscriptionDurationMs(Long transcriptionDurationMs) {
        this.transcriptionDurationMs = transcriptionDurationMs;
    }
}
