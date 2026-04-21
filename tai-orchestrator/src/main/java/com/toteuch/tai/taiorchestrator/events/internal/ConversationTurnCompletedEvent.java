package com.toteuch.tai.taiorchestrator.events.internal;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Internal orchestrator event emitted when a full conversation turn is considered complete.
 *
 * <p>A completed turn usually means that the user input has been processed,
 * the assistant reply has been generated, and any optional TTS playback has finished
 * or has been intentionally skipped.</p>
 *
 * @param userText      the final user message associated with the completed turn
 * @param assistantText the final assistant reply associated with the completed turn
 * @param completedAt   the timestamp at which the turn was considered complete
 */
public record ConversationTurnCompletedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String userText,
    String assistantText,
    Instant completedAt
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.CONVERSATION_TURN_COMPLETED;
    }
}
