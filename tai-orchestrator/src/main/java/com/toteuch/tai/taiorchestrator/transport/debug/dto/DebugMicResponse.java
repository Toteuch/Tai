package com.toteuch.tai.taiorchestrator.transport.debug.dto;

public record DebugMicResponse(
    boolean success,
    String sessionId,
    boolean recording,
    String audioFile,
    String transcribedText,
    String language,
    Double languageProbability,
    boolean injectedIntoOrchestrator,
    String errorCode,
    String errorMessage
) {
}
