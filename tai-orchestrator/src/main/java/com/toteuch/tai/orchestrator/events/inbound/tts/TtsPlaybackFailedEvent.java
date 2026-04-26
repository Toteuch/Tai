package com.toteuch.tai.orchestrator.events.inbound.tts;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

/**
 * Event emitted when the TTS service fails to start or complete playback.
 *
 * <p>This event allows the orchestrator to recover from speech output errors
 * without losing the already generated assistant text. The text reply may still
 * remain visible in the UI even if spoken playback fails.</p>
 *
 * @param errorCode    a machine-readable error code describing the failure
 * @param errorMessage a human-readable error message describing the failure
 */
public record TtsPlaybackFailedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String errorCode,
        String errorMessage)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.TTS_PLAYBACK_FAILED;
    }
}
