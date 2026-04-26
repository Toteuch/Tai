package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;

import java.time.Instant;

public record AssistantReplyFailedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    String errorCode,
    String errorMessage
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.ASSISTANT_REPLY_FAILED;
    }
}
