// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.stt;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record SttSpeechStartedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        Double averageEnergy,
        Double peakEnergy)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.STT_SPEECH_STARTED;
    }
}
