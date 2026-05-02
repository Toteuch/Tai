package com.toteuch.tai.orchestrator.transport.events.tts;

import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventRequest;

public class TtsPlaybackFailedEventRequest extends AbstractTransportEventRequest {
    private String errorCode;
    private String errorMessage;
    private Long speechDurationMs;

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

    public Long getSpeechDurationMs() {
        return speechDurationMs;
    }

    public void setSpeechDurationMs(Long speechDurationMs) {
        this.speechDurationMs = speechDurationMs;
    }
}
