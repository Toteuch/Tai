package com.toteuch.tai.orchestrator.transport.events.stt;

public class SttTranscriptAcceptedEventRequest extends AbstractSttTransportEventRequest {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
