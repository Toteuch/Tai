package com.toteuch.tai.taiorchestrator.events.inbound;

import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.events.EventSource;

import java.time.Instant;

/**
 * Event emitted when the TTS service has fully completed audio playback.
 *
 * <p>This event marks the end of the spoken assistant reply for a conversation turn.
 * It is typically used to return the orchestrator to an idle speaking state
 * and finalize the turn lifecycle.</p>
 *
 * @param text the text that was spoken, if echoed back by the TTS service
 * @param durationMs the playback duration in milliseconds, if available
 */
public record TtsPlaybackCompletedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String text,
    Long durationMs
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.TTS_PLAYBACK_COMPLETED;
    }
}
