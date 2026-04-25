package com.toteuch.tai.taiorchestrator.transport.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public abstract class AbstractTransportEventMapper {
    private final static Logger errorLog = LoggerFactory.getLogger("tai.error");

    protected String safeId(String id) {
        if (id == null) {
            id = UUID.randomUUID().toString();
            errorLog.warn("Transport event id is null, generating a random id");
        }
        return id;
    }

    protected Instant safeTime(Instant t) {
        if (t == null) {
            t = Instant.now();
            errorLog.warn("Transport event createdAt is null, set it to now");
        }
        return t;
    }

    protected String safeCorrelation(String correlationId) {
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            errorLog.warn("Transport event correlationId is null, generating a random correlationId");
        }
        return correlationId;
    }
}
