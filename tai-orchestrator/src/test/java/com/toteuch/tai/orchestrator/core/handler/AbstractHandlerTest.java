package com.toteuch.tai.orchestrator.core.handler;

import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractHandlerTest {

    public static final String MODEL_NAME = "tai-model";
    protected final CapturingTaiEventPublisher eventPublisher = new CapturingTaiEventPublisher();

    protected SessionStore fixedSessionStore(SessionContext sessionContext) {
        return () -> sessionContext;
    }

    protected void publishLlmCompletedEvent(String correlationId, String text) {
        eventPublisher.publish(new LlmResponseCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            correlationId,
            EventSource.LLM_SERVICE,
            text,
            MODEL_NAME,
            1,
            2,
            100L
        ));
    }

    protected void publishLlmFailedEvent(String correlationId) {
        eventPublisher.publish(new LlmResponseFailedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            correlationId,
            EventSource.LLM_SERVICE,
            "LLM_ERROR",
            "LLM failed"
        ));
    }

    protected static class CapturingTaiEventPublisher implements TaiEventPublisher {

        private final List<TaiEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publish(TaiEvent event) {
            publishedEvents.add(event);
        }

        public List<TaiEvent> publishedEvents() {
            return publishedEvents;
        }

        public void assertNoEventPublished() {
            assertThat(publishedEvents).isEmpty();
        }

        public <T extends TaiEvent> T assertSingleEventPublished(Class<T> expectedType) {
            assertThat(publishedEvents).hasSize(1);
            assertThat(publishedEvents.getFirst()).isInstanceOf(expectedType);
            return expectedType.cast(publishedEvents.getFirst());
        }
    }
}
