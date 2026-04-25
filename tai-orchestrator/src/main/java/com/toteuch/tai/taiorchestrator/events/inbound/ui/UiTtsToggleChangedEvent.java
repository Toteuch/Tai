package com.toteuch.tai.taiorchestrator.events.inbound.ui;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

import java.time.Instant;

/**
 * Event emitted when the UI changes the Text-to-Speech enabled state.
 *
 * <p>This event updates the runtime behavior of the orchestrator by enabling
 * or disabling spoken assistant replies.</p>
 *
 * @param enabled whether TTS should be enabled after this change
 */
public record UiTtsToggleChangedEvent(
    String eventId,
    Instant occurredAt,
    String correlationId,
    EventSource source,
    boolean enabled
) implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.UI_TTS_TOGGLE_CHANGED;
    }
}
