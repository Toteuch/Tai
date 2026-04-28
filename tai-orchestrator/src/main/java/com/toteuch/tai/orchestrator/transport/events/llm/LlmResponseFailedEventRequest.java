// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport.events.llm;

import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventRequest;

public class LlmResponseFailedEventRequest extends AbstractTransportEventRequest {
    private String modelName;
    private String errorCode;
    private String errorMessage;
    private Long generationDurationMs;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getGenerationDurationMs() {
        return generationDurationMs;
    }

    public void setGenerationDurationMs(Long generationDurationMs) {
        this.generationDurationMs = generationDurationMs;
    }
}
