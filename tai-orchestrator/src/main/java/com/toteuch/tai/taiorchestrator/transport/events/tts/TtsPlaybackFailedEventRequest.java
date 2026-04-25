package com.toteuch.tai.taiorchestrator.transport.events.tts;

import com.toteuch.tai.taiorchestrator.transport.events.AbstractTransportEventRequest;

public class TtsPlaybackFailedEventRequest extends AbstractTransportEventRequest {
    private String errorCode;
    private String errorMessage;

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
}
