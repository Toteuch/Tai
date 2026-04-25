package com.toteuch.tai.taiorchestrator.transport.debug.dto;

public record DebugSttProcessResponse(
    boolean success,
    String transcribedText,
    String language,
    Double languageProbability,
    boolean injectedIntoOrchestrator,
    String errorCode,
    String errorMessage
) {
}
