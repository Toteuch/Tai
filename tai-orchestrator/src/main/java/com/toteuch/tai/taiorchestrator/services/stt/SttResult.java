package com.toteuch.tai.taiorchestrator.services.stt;

public record SttResult(
    boolean success,
    String text,
    String language,
    Double languageProbability,
    String errorCode,
    String errorMessage
) {
}
