// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.events.inbound.ui;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;
import java.time.Instant;

public record UiManualTextInputReceivedEvent(
        String eventId, Instant occurredAt, String correlationId, EventSource source, String text)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.UI_MANUAL_TEXT_INPUT_RECEIVED;
    }
}
