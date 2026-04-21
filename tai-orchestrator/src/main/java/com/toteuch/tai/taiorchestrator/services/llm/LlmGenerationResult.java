package com.toteuch.tai.taiorchestrator.services.llm;

public record LlmGenerationResult(
    boolean success,
    String responseText,
    String modelName,
    Integer inputTokens,
    Integer outputTokens,
    Long generationDurationMs,
    String errorCode,
    String errorMessage,
    boolean retryable
) {
}
