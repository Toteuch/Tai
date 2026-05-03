// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.llm;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record LlmResponseFailedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String modelName,
        Long generationDurationMs,
        String errorCode,
        String errorMessage)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.LLM_RESPONSE_FAILED;
    }
}
