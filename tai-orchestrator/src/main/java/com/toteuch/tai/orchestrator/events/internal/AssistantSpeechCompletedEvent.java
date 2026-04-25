package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;

import java.time.Instant;

public record AssistantSpeechCompletedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.ASSISTANT_SPEECH_COMPLETED;
    }
}
