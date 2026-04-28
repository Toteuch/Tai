// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.transport.dto;

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
