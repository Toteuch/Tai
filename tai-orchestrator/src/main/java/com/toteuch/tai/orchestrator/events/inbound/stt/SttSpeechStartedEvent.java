package com.toteuch.tai.orchestrator.events.inbound.stt;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

public record SttSpeechStartedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        Long durationMs,
        Double averageEnergy)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.STT_SPEECH_STARTED;
    }
}
