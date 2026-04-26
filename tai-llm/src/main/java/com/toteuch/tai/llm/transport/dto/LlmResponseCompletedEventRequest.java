package com.toteuch.tai.llm.transport.dto;

public class LlmResponseCompletedEventRequest extends AbstractTransportEventRequest {
    private String responseText, modelName;
    private Integer inputTokens, outputTokens;
    private Long generationDurationMs;

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String v) {
        responseText = v;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String v) {
        modelName = v;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer v) {
        inputTokens = v;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer v) {
        outputTokens = v;
    }

    public Long getGenerationDurationMs() {
        return generationDurationMs;
    }

    public void setGenerationDurationMs(Long v) {
        generationDurationMs = v;
    }
}
