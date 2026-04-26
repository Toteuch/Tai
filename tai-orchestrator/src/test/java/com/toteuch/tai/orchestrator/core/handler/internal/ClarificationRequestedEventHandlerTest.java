package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.ClarificationRequestedEvent;
import com.toteuch.tai.orchestrator.services.llm.LlmClient;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClarificationRequestedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_create_non_persistent_turn_call_llm_and_publish_completed_event() {
        SessionContext context = new SessionContext();

        LlmClient llmClient = mock(LlmClient.class);

        ClarificationRequestedEventHandler handler = new ClarificationRequestedEventHandler(
            fixedSessionStore(context),
            llmClient
        );

        handler.handle(new ClarificationRequestedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.ORCHESTRATOR
        ));

        assertThat(context.getActiveTurn()).isNotNull();
        assertThat(context.getActiveTurn().getCorrelationId()).isEqualTo("corr-1");
        assertThat(context.getActiveTurn().getUserMessage()).isEqualTo("...");
        assertThat(context.getActiveTurn().isPersistInHistory()).isFalse();
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.GENERATING);

        verify(llmClient).generateReply(eq("corr-1"), anyList());
        publishLlmCompletedEvent("corr-1", "Can you say that again?");

        LlmResponseCompletedEvent published =
            eventPublisher.assertSingleEventPublished(LlmResponseCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.responseText()).isEqualTo("Can you say that again?");
    }

    @Test
    void should_publish_failed_event_when_clarification_llm_generation_fails() {
        SessionContext context = new SessionContext();

        LlmClient llmClient = mock(LlmClient.class);

        ClarificationRequestedEventHandler handler = new ClarificationRequestedEventHandler(
            fixedSessionStore(context),
            llmClient
        );

        handler.handle(new ClarificationRequestedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.ORCHESTRATOR
        ));

        verify(llmClient).generateReply(eq("corr-1"), anyList());
        publishLlmFailedEvent("corr-1");

        LlmResponseFailedEvent published =
            eventPublisher.assertSingleEventPublished(LlmResponseFailedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.errorCode()).isEqualTo("LLM_ERROR");
    }
}
