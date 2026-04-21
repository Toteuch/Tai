package com.toteuch.tai.taiorchestrator.core;

public interface UserInputProcessor {
    void processUserText(
        String sessionId,
        String correlationId,
        String userText,
        boolean interruption
    );
}
