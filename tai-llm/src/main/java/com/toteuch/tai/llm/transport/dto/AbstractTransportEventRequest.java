package com.toteuch.tai.llm.transport.dto;

import java.time.Instant;

public abstract class AbstractTransportEventRequest {
    private String eventId;
    private Instant createdAt;
    private TransportEventSource source;
    private String correlationId;
    private String modelName;
    private Long generationDurationMs;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String v) {
        eventId = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant v) {
        createdAt = v;
    }

    public TransportEventSource getSource() {
        return source;
    }

    public void setSource(TransportEventSource v) {
        source = v;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String v) {
        correlationId = v;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Long getGenerationDurationMs() {
        return generationDurationMs;
    }

    public void setGenerationDurationMs(Long generationDurationMs) {
        this.generationDurationMs = generationDurationMs;
    }
}
