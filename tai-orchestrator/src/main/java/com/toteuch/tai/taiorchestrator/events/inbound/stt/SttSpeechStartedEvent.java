package com.toteuch.tai.taiorchestrator.events.inbound.stt;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

public record SttSpeechStartedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    Long durationMs,
    Double averageEnergy
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.STT_SPEECH_STARTED;
    }
}
