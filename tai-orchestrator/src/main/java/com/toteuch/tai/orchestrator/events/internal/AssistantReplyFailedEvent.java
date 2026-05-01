// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record AssistantReplyFailedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String errorCode,
        String errorMessage,
        Long llmGenerationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.ASSISTANT_REPLY_FAILED;
    }
}
