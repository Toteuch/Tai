// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.tts;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record TtsPlaybackStartedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String text,
        Long synthesisDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.TTS_PLAYBACK_STARTED;
    }
}
