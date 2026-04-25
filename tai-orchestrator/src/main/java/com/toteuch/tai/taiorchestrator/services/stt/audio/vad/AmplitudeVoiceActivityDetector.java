package com.toteuch.tai.taiorchestrator.services.stt.audio.vad;

import org.springframework.stereotype.Component;

@Component
public class AmplitudeVoiceActivityDetector implements VoiceActivityDetector {

    private final VoiceActivityDetectorProperties properties;

    private boolean speechStarted;
    private boolean speechEnded;
    private long speechStartTimeMs;
    private long silenceStartTimeMs = -1L;
    private long totalSamples;
    private long totalEnergy;

    public AmplitudeVoiceActivityDetector(VoiceActivityDetectorProperties properties) {
        this.properties = properties;
    }

    @Override
    public void reset() {
        speechStarted = false;
        speechEnded = false;
        speechStartTimeMs = 0L;
        silenceStartTimeMs = -1L;
        totalSamples = 0L;
        totalEnergy = 0L;
    }

    @Override
    public void onAudioChunk(byte[] buffer, int bytesRead) {
        long now = System.currentTimeMillis();
        int level = calculateAudioLevel(buffer, bytesRead);

        if (bytesRead > 0) {
            totalSamples++;
            totalEnergy += level;
        }

        if (!speechStarted && level >= properties.getSpeechThreshold()) {
            speechStarted = true;
            speechStartTimeMs = now;
            silenceStartTimeMs = -1L;
            return;
        }

        if (!speechStarted) {
            return;
        }

        if (level >= properties.getSpeechThreshold()) {
            silenceStartTimeMs = -1L;
            return;
        }

        if (silenceStartTimeMs < 0) {
            silenceStartTimeMs = now;
            return;
        }

        long speechDuration = now - speechStartTimeMs;
        long silenceDuration = now - silenceStartTimeMs;

        if (speechDuration >= properties.getMinSpeechDurationMs()
            && silenceDuration >= properties.getSilenceDurationMs()) {
            speechEnded = true;
        }
    }

    @Override
    public boolean isSpeechStarted() {
        return speechStarted;
    }

    @Override
    public boolean isSpeechEnded() {
        return speechEnded;
    }

    @Override
    public double getAverageEnergy() {
        if (totalSamples == 0L) {
            return 0.0;
        }
        return (double) totalEnergy / (double) totalSamples;
    }

    @Override
    public long getSpeechDurationMs() {
        if (!speechStarted) {
            return 0L;
        }

        long now = System.currentTimeMillis();
        return Math.max(0L, now - speechStartTimeMs);
    }

    private int calculateAudioLevel(byte[] buffer, int bytesRead) {
        int max = 0;

        for (int i = 0; i < bytesRead - 1; i += 2) {
            int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xff);
            max = Math.max(max, Math.abs(sample));
        }

        return max;
    }
}
