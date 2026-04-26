package com.toteuch.tai.tts.piper.transport.dto;

public class TtsPlaybackFailedEventRequest extends AbstractTransportEventRequest {
    private String errorCode;
    private String errorMessage;

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
