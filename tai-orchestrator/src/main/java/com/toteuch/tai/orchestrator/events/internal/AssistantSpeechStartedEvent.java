package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

public record AssistantSpeechStartedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        Long synthesisDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.ASSISTANT_SPEECH_STARTED;
    }
}
