package com.toteuch.tai.orchestrator.events.inbound.stt;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

public record SttTranscriptAcceptedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String text,
        String language,
        Double languageProbability,
        Long durationMs,
        Double averageEnergy,
        String reason,
        Integer suspicionScore)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.STT_TRANSCRIPT_ACCEPTED;
    }
}
