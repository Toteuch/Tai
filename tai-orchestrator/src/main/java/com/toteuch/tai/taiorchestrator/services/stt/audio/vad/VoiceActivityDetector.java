package com.toteuch.tai.taiorchestrator.services.stt.audio.vad;

public interface VoiceActivityDetector {

    void reset();

    void onAudioChunk(byte[] buffer, int bytesRead);

    boolean isSpeechStarted();

    boolean isSpeechEnded();

    double getAverageEnergy();

    long getSpeechDurationMs();
}
