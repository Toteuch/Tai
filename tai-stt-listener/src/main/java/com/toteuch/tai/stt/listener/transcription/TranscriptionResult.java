// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.transcription;

public record TranscriptionResult(
        boolean success,
        String correlationId,
        String text,
        String language,
        Double languageProbability,
        Long transcriptionDurationMs,
        String modelName,
        String errorCode,
        String errorMessage) {
    public static TranscriptionResult success(
            String text, String language, Double languageProbability) {
        return new TranscriptionResult(
                true, null, text, language, languageProbability, null, null, null, null);
    }

    public static TranscriptionResult failure(String errorCode, String errorMessage) {
        return new TranscriptionResult(
                false, null, null, null, null, null, null, errorCode, errorMessage);
    }
}
