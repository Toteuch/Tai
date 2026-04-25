package com.toteuch.tai.taiorchestrator.transport.events.llm;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.taiorchestrator.transport.events.AbstractTransportEventMapper;
import org.springframework.stereotype.Component;

@Component
public class LlmTransportEventMapper extends AbstractTransportEventMapper {

    public TaiEvent toEvent(LlmResponseCompletedEventRequest req) {
        return new LlmResponseCompletedEvent(
            safeId(req.getEventId()),
            safeTime(req.getCreatedAt()),
            safeCorrelation(req.getCorrelationId()),
            EventSource.LLM_SERVICE,
            req.getResponseText(),
            req.getModelName(),
            req.getInputTokens(),
            req.getOutputTokens(),
            req.getGenerationDurationMs()
        );
    }

    public TaiEvent toEvent(LlmResponseFailedEventRequest req) {
        return new LlmResponseFailedEvent(
            safeId(req.getEventId()),
            safeTime(req.getCreatedAt()),
            safeCorrelation(req.getCorrelationId()),
            EventSource.LLM_SERVICE,
            req.getErrorCode(),
            req.getErrorMessage()
        );
    }
}
