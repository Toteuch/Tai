package com.toteuch.tai.tts.piper.transport.dto;

public class TtsPlaybackStartedEventRequest extends AbstractTransportEventRequest {
    private String text;
    private String voiceId;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getVoiceId() { return voiceId; }
    public void setVoiceId(String voiceId) { this.voiceId = voiceId; }
}
