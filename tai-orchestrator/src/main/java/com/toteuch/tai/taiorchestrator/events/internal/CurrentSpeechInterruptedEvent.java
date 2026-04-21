package com.toteuch.tai.taiorchestrator.events.internal;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Internal orchestrator event emitted when the assistant's current speech output
 * is interrupted in order to handle a new incoming user utterance.
 *
 * <p>This event is mainly used to support barge-in behavior, where a new user input
 * takes priority over an ongoing spoken response.</p>
 *
 * @param reason the functional reason for the interruption
 * @param interruptedCorrelationId the correlation identifier of the interrupted reply flow
 */
public record CurrentSpeechInterruptedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String reason,
    String interruptedCorrelationId
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.CURRENT_SPEECH_INTERRUPTED;
    }
}
