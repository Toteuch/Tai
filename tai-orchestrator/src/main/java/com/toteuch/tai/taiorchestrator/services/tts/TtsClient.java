package com.toteuch.tai.taiorchestrator.services.tts;

public interface TtsClient {
    void speak(String correlationId, String text);

    void stop(String correlationId);
}
