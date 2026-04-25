package com.toteuch.tai.taiorchestrator.core.handler.inbound.llm;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.internal.AssistantReplyAcceptedEvent;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class LlmResponseCompletedEventHandler implements EventHandler<LlmResponseCompletedEvent> {
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public LlmResponseCompletedEventHandler(
        SessionStore sessionStore,
        TaiEventPublisher eventPublisher
    ) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.LLM_RESPONSE_COMPLETED;
    }

    @Override
    public void handle(LlmResponseCompletedEvent event) {
        perfLog.info("LLM generation completed | correlationId={} generationDurationMs={}",
            event.correlationId(),
            event.generationDurationMs()
        );

        SessionContext sessionContext = sessionStore.get();

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info("{} ignored | correlationId={} activeTurnCorrelationId={}",
                this.getClass().getSimpleName(),
                event.correlationId(),
                sessionContext.getActiveTurn() != null ? sessionContext.getActiveTurn().getCorrelationId() : null);
            return;
        }

        eventPublisher.publish(new AssistantReplyAcceptedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            event.correlationId(),
            EventSource.LLM_SERVICE,
            event.responseText()
        ));
    }
}
