package com.toteuch.tai.stt.listener.api.dto;

import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;

public record TranscriptionResponse(
    boolean success,
    String correlationId,
    String text,
    String language,
    Double languageProbability,
    Long transcriptionDurationMs,
    String modelName,
    String errorCode,
    String errorMessage
) {
    public static TranscriptionResponse from(TranscriptionResult result) {
        if (result == null) {
            return null;
        }

        return new TranscriptionResponse(
            result.success(),
            result.correlationId(),
            result.text(),
            result.language(),
            result.languageProbability(),
            result.transcriptionDurationMs(),
            result.modelName(),
            result.errorCode(),
            result.errorMessage()
        );
    }
}
