package com.toteuch.tai.orchestrator.events.inbound.tts;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Event emitted when the TTS service has fully completed audio playback.
 *
 * <p>This event marks the end of the spoken assistant reply for a conversation turn.
 * It is typically used to return the orchestrator to an idle speaking state
 * and finalize the turn lifecycle.</p>
 *
 * @param text             the text that was spoken, if echoed back by the TTS service
 * @param speechDurationMs the playback duration in milliseconds, if available
 */
public record TtsPlaybackCompletedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    String text,
    Long speechDurationMs
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.TTS_PLAYBACK_COMPLETED;
    }
}
