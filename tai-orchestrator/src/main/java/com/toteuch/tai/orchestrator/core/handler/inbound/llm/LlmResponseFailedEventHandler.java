package com.toteuch.tai.orchestrator.core.handler.inbound.llm;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyFailedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
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
        perfLog.info("LLM generation failed | correlationId={}", event.correlationId());
        SessionContext sessionContext = sessionStore.get();

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info(
                    "{} ignored | correlationId={} activeTurnCorrelationId={}",
                    this.getClass().getSimpleName(),
                    event.correlationId(),
                    sessionContext.getActiveTurn().getCorrelationId());
            return;
        }

        eventPublisher.publish(
                new AssistantReplyFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        event.correlationId(),
                        EventSource.LLM_SERVICE,
                        event.errorCode(),
                        event.errorMessage()));
    }
}
