// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.llm;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyFailedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LlmResponseFailedEventHandler implements EventHandler<LlmResponseFailedEvent> {
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public LlmResponseFailedEventHandler(
            SessionStore sessionStore, TaiEventPublisher eventPublisher) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.LLM_RESPONSE_FAILED;
    }

    @Override
    public void handle(LlmResponseFailedEvent event) {
        perfLog.debug(
                "LLM generation completed | correlationId={} modelName={}",
                event.correlationId(),
                event.modelName());
        SessionContext sessionContext = sessionStore.get();

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info(
                    "{} ignored | correlationId={} activeTurnCorrelationId={}",
                    event.getClass().getSimpleName(),
                    event.correlationId(),
                    sessionContext.getActiveTurn().getCorrelationId());
            return;
        }

        eventPublisher.publish(
                new AssistantReplyFailedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source(),
                        event.errorCode(),
                        event.errorMessage(),
                        event.generationDurationMs()));
    }
}
