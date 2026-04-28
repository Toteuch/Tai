// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

public record ClarificationRequestedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        Long transcriptDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.CLARIFICATION_REQUESTED;
    }
}
