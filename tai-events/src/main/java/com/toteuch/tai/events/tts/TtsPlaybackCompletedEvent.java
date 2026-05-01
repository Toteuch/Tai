// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.tts;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record TtsPlaybackCompletedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String text,
        Long speechDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.TTS_PLAYBACK_COMPLETED;
    }
}
