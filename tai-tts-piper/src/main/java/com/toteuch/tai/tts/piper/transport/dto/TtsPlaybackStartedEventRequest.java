// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.transport.dto;

public class TtsPlaybackStartedEventRequest extends AbstractTransportEventRequest {
    private String text;
    private String voiceId;
    private Long synthesisDurationMs;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public Long getSynthesisDurationMs() {
        return synthesisDurationMs;
    }

    public void setSynthesisDurationMs(Long synthesisDurationMs) {
        this.synthesisDurationMs = synthesisDurationMs;
    }
}
