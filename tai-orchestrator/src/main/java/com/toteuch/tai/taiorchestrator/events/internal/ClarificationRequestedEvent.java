package com.toteuch.tai.taiorchestrator.events.internal;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

public record ClarificationRequestedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.CLARIFICATION_REQUESTED;
    }
}
