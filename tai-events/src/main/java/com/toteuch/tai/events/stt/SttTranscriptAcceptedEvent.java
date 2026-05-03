// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.stt;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record SttTranscriptAcceptedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        String transcript,
        String language,
        Double languageProbability,
        Long userSpeechDurationMs,
        Long transcriptionDurationMs)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.STT_TRANSCRIPT_ACCEPTED;
    }
}
