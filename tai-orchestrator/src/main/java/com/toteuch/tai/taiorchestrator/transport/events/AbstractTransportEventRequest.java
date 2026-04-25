package com.toteuch.tai.taiorchestrator.transport.events;

import java.time.Instant;

public abstract class AbstractTransportEventRequest {

    private String eventId;
    private TransportEventType eventType;
    private Instant timestamp;
    private TransportEventSource source;
    private String version;
    private String correlationId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public TransportEventType getEventType() {
        return eventType;
    }

    public void setEventType(TransportEventType eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public TransportEventSource getSource() {
        return source;
    }

    public void setSource(TransportEventSource source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
