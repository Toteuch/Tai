package com.toteuch.tai.taiorchestrator.services.stt.audio.vad;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.stt.vad")
public class VoiceActivityDetectorProperties {

    private int speechThreshold = 500;
    private long silenceDurationMs = 1200;
    private long minSpeechDurationMs = 300;
    private float sampleRate = 16000.0f;

    public int getSpeechThreshold() {
        return speechThreshold;
    }

    public void setSpeechThreshold(int speechThreshold) {
        this.speechThreshold = speechThreshold;
    }

    public long getSilenceDurationMs() {
        return silenceDurationMs;
    }

    public void setSilenceDurationMs(long silenceDurationMs) {
        this.silenceDurationMs = silenceDurationMs;
    }

    public long getMinSpeechDurationMs() {
        return minSpeechDurationMs;
    }

    public void setMinSpeechDurationMs(long minSpeechDurationMs) {
        this.minSpeechDurationMs = minSpeechDurationMs;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }
}
