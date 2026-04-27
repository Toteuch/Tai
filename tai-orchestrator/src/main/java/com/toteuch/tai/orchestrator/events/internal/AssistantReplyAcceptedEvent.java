package com.toteuch.tai.orchestrator.events.internal;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

/**
 * Internal orchestrator event emitted when a generated LLM reply has been accepted
 * as the official assistant response for the current turn.
 *
 * <p>This event is emitted after generation succeeds and before optional TTS playback.
 * It marks the point where the assistant reply becomes part of the conversation state.</p>
 *
 * @param replyText the accepted assistant reply text
 */
public record AssistantReplyAcceptedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String replyText,
        Long llmGenerationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.ASSISTANT_REPLY_ACCEPTED;
    }
}
