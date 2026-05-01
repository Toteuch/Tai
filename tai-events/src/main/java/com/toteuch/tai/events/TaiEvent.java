// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.events;

import java.time.Instant;

public interface TaiEvent {

    /**
     * Unique identifier of the event.
     */
    String eventId();

    /**
     * Timestamp at which the event occurred.
     */
    Instant occurredAt();

    /**
     * Identifier used to correlate events belonging to the same conversation turn.
     */
    String correlationId();

    /**
     * Source of the event (STT, LLM, TTS...).
     */
    EventSource source();

    /**
     * Type of the event.
     */
    EventType type();
}
