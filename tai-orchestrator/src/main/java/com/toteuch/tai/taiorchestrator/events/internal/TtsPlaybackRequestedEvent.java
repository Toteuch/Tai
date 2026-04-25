package com.toteuch.tai.taiorchestrator.events.internal;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Internal orchestrator event emitted when a text reply must be sent to the TTS service
 * for spoken playback.
 *
 * <p>This event is created only when speech output is enabled and a valid assistant
 * reply has already been accepted.</p>
 *
 * @param text    the reply text that should be spoken
 * @param voiceId the target voice identifier to use, if one is explicitly selected
 */
public record TtsPlaybackRequestedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String text,
    String voiceId
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.TTS_PLAYBACK_REQUESTED;
    }
}
