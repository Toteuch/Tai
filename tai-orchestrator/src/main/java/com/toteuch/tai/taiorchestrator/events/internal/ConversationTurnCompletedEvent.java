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
 */
public record ConversationTurnCompletedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.CONVERSATION_TURN_COMPLETED;
    }
}
