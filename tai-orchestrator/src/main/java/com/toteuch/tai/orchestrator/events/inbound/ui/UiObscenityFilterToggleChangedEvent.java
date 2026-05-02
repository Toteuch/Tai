package com.toteuch.tai.orchestrator.events.inbound.ui;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.time.Instant;

/**
 * Event emitted when the UI changes the obscenity filter enabled state.
 *
 * <p>This event updates the runtime content policy applied by the orchestrator
 * or downstream processing pipeline.</p>
 *
 * @param enabled whether the obscenity filter should be enabled after this change
 */
public record UiObscenityFilterToggleChangedEvent(
        String eventId,
        Instant occurredAt,
        String correlationId,
        EventSource source,
        boolean enabled)
        implements TaiEvent {
    @Override
    public EventType type() {
        return EventType.UI_OBSCENITY_FILTER_TOGGLE_CHANGED;
    }
}
