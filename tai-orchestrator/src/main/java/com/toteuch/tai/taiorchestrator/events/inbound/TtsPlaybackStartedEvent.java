package com.toteuch.tai.taiorchestrator.events.inbound;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Event emitted when the TTS service has effectively started audio playback.
 *
 * <p>This event confirms that the assistant has moved from reply preparation
 * to actual speech output. It is mainly used to update speaking state and UI.</p>
 *
 * @param text    the text being spoken, if echoed back by the TTS service
 * @param voiceId the identifier of the voice used for playback, if available
 */
public record TtsPlaybackStartedEvent(
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
        return EventType.TTS_PLAYBACK_STARTED;
    }
}
