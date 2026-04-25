package com.toteuch.tai.stt.listener.transcription;

public record TranscriptionResult(
    boolean success,
    String text,
    String language,
    Double languageProbability,
    String errorCode,
    String errorMessage
) {
    public static TranscriptionResult success(String text, String language, Double languageProbability) {
        return new TranscriptionResult(true, text, language, languageProbability, null, null);
    }

    public static TranscriptionResult failure(String errorCode, String errorMessage) {
        return new TranscriptionResult(false, null, null, null, errorCode, errorMessage);
    }
}
