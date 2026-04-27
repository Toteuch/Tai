package com.toteuch.tai.orchestrator.transport.events.llm;

import com.toteuch.tai.orchestrator.events.TaiEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventMapper;
import org.springframework.stereotype.Component;

@Component
public class LlmTransportEventMapper extends AbstractTransportEventMapper {

    public TaiEvent toEvent(LlmResponseCompletedEventRequest req) {
        return new LlmResponseCompletedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getResponseText(),
                req.getModelName(),
                req.getInputTokens(),
                req.getOutputTokens(),
                req.getGenerationDurationMs());
    }

    public TaiEvent toEvent(LlmResponseFailedEventRequest req) {
        return new LlmResponseFailedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getModelName(),
                req.getErrorCode(),
                req.getErrorMessage(),
                req.getGenerationDurationMs());
    }
}
