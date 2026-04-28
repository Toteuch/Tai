// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.transport.dto;

public class LlmResponseFailedEventRequest extends AbstractTransportEventRequest {
    private String errorCode, errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String v) {
        errorCode = v;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String v) {
        errorMessage = v;
    }
}
