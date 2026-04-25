package com.toteuch.tai.taiorchestrator.events.internal;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Internal orchestrator event emitted when a user utterance has been validated
 * and accepted as the next conversational input to process.
 *
 * <p>This event represents the moment when an external input, such as STT output
 * or manual text input, becomes an official user message in the conversation flow.</p>
 *
 * @param transcript the accepted user utterance text
 */
public record UserUtteranceAcceptedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String transcript
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.USER_UTTERANCE_ACCEPTED;
    }
}
