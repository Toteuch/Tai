// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.llm;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record LlmResponseCompletedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String responseText,
        String modelName,
        Integer inputTokens,
        Integer outputTokens,
        Long generationDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.LLM_RESPONSE_COMPLETED;
    }
}
