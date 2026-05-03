// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record AssistantSpeechCompletedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        Long speechDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.ASSISTANT_SPEECH_COMPLETED;
    }
}
