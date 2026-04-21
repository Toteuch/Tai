package com.toteuch.tai.taiorchestrator.events.internal;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;

import java.time.Instant;
import java.util.List;

/**
 * Internal orchestrator event emitted when a new LLM generation must be started.
 *
 * <p>This event represents the transition from accepted conversational input
 * to model invocation. It carries the fully assembled prompt context and the
 * target model information required for generation.</p>
 *
 * @param messages the ordered message list sent to the LLM
 * @param model the target model name to use for generation
 * @param requestId the technical identifier of the generation request
 */
public record LlmGenerationRequestedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    List<LlmMessage> messages,
    String model,
    String requestId
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.LLM_GENERATION_REQUESTED;
    }
}
