package com.toteuch.tai.taiorchestrator.events.inbound.ui;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

public record UiManualTextInputReceivedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    String text
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.UI_MANUAL_TEXT_INPUT_RECEIVED;
    }
}
