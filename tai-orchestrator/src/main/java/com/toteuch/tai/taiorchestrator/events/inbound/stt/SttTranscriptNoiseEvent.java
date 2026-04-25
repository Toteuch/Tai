package com.toteuch.tai.taiorchestrator.events.inbound.stt;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

public record SttTranscriptNoiseEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    Long durationMs,
    Double averageEnergy,
    String reason,
    Integer suspicionScore
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.STT_TRANSCRIPT_NOISE;
    }
}
