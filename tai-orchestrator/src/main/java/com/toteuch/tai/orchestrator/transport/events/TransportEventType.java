package com.toteuch.tai.orchestrator.transport.events;

public enum TransportEventType {
    STT_SPEECH_STARTED,
    STT_TRANSCRIPT_ACCEPTED,
    STT_TRANSCRIPT_UNINTELLIGIBLE,
    STT_TRANSCRIPT_NOISE,
    LLM_RESPONSE_COMPLETED,
    LLM_RESPONSE_FAILED
}
