package com.toteuch.tai.llm.transport.dto;

public class LlmResponseCompletedEventRequest extends AbstractTransportEventRequest {
    private String responseText;
    private Integer inputTokens, outputTokens;

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String v) {
        responseText = v;
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
}
