package com.toteuch.tai.llm.ollama;

public record OllamaGenerationResult(
        boolean success,
        String responseText,
        String modelName,
        Integer inputTokens,
        Integer outputTokens,
        Long generationDurationMs,
        String errorCode,
        String errorMessage) {
    public static OllamaGenerationResult success(
            String text, String model, Integer in, Integer out, Long ms) {
        return new OllamaGenerationResult(true, text, model, in, out, ms, null, null);
    }

    public static OllamaGenerationResult failure(String model, String code, String msg, Long ms) {
        return new OllamaGenerationResult(false, null, model, null, null, ms, code, msg);
    }
}
