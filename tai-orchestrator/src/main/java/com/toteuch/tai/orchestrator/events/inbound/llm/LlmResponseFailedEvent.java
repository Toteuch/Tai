package com.toteuch.tai.orchestrator.events.inbound.llm;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Event emitted when the LLM service fails to generate a reply.
 *
 * <p>This event allows the orchestrator to leave the generating state,
 * report the failure to the UI, and decide whether the failed request
 * can be retried or should be abandoned.</p>
 *
 * @param errorCode    a machine-readable error code describing the failure
 * @param errorMessage a human-readable error message describing the failure
 */
public record LlmResponseFailedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    String errorCode,
    String errorMessage
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.LLM_RESPONSE_FAILED;
    }
}
