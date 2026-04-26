package com.toteuch.tai.stt.listener.api.dto;

public record CaptureDebugResponse(
    boolean success,
    SpeechSegmentResponse segment
) {
}
