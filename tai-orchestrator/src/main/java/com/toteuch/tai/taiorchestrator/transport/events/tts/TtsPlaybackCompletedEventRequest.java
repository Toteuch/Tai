package com.toteuch.tai.taiorchestrator.transport.events.tts;

import com.toteuch.tai.taiorchestrator.transport.events.AbstractTransportEventRequest;

public class TtsPlaybackCompletedEventRequest extends AbstractTransportEventRequest {
    private String text;
    private Long speechDurationMs;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getSpeechDurationMs() {
        return speechDurationMs;
    }

    public void setSpeechDurationMs(Long speechDurationMs) {
        this.speechDurationMs = speechDurationMs;
    }
}
