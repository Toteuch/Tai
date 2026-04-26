package com.toteuch.tai.orchestrator.transport.events.tts;

import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventRequest;

public class TtsPlaybackStartedEventRequest extends AbstractTransportEventRequest {
    private String text;
    private String voiceId;

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
}
