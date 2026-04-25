package com.toteuch.tai.taiorchestrator.transport.events;

import java.time.Instant;

public abstract class AbstractTransportEventRequest {

    private String eventId;
    private Instant createdAt;
    private TransportEventSource source;
    private String correlationId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public TransportEventSource getSource() {
        return source;
    }

    public void setSource(TransportEventSource source) {
        this.source = source;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
