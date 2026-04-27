package com.toteuch.tai.orchestrator.transport.events;

import com.toteuch.tai.orchestrator.events.EventSource;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransportEventMapper {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");
    private static final Map<TransportEventSource, EventSource> eventSourceMap =
            Map.of(
                    TransportEventSource.LLM_SERVICE, EventSource.LLM_SERVICE,
                    TransportEventSource.UI_SERVICE, EventSource.UI,
                    TransportEventSource.STT_SERVICE, EventSource.STT_SERVICE,
                    TransportEventSource.TTS_SERVICE, EventSource.TTS_SERVICE,
                    TransportEventSource.UNKNOWN, EventSource.UNKNOWN);

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
            errorLog.warn(
                    "Transport event correlationId is null, generating a random correlationId");
        }
        return correlationId;
    }

    protected EventSource mapEventSource(TransportEventSource transportEventSource) {
        EventSource eventSource = eventSourceMap.get(transportEventSource);
        if (eventSource == null) {
            errorLog.error(
                    "event source mapping fallback | transport event source: {}",
                    transportEventSource);
            eventSource = EventSource.UNKNOWN;
        }
        return eventSource;
    }
}
