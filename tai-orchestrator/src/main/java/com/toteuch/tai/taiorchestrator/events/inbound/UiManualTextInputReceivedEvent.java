package com.toteuch.tai.taiorchestrator.events.inbound;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Event emitted when the UI sends a manual text input to the orchestrator.
 *
 * <p>This event provides a non-voice entry path into the conversation flow.
 * It is particularly useful for development, testing, and fallback interaction modes.</p>
 *
 * @param text the manually entered user message
 */
public record UiManualTextInputReceivedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String text
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.UI_MANUAL_TEXT_INPUT_RECEIVED;
    }
}
