// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport.events.llm;

import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventRequest;

public class LlmResponseCompletedEventRequest extends AbstractTransportEventRequest {
    private String responseText;
    private String modelName;
    private Integer inputTokens;
    private Integer outputTokens;
    private Long generationDurationMs;

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public Long getGenerationDurationMs() {
        return generationDurationMs;
    }

    public void setGenerationDurationMs(Long generationDurationMs) {
        this.generationDurationMs = generationDurationMs;
    }
}
