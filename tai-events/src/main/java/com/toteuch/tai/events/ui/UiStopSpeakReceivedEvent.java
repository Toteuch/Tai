// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events.ui;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record UiStopSpeakReceivedEvent(
        String eventId, Instant occurredAt, String correlationId, EventSource source)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.UI_STOP_SPEAK_RECEIVED;
    }
}
