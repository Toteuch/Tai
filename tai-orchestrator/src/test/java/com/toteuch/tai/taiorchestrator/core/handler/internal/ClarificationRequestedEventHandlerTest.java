package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.taiorchestrator.events.internal.ClarificationRequestedEvent;
import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.ThinkingState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClarificationRequestedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_create_non_persistent_turn_call_llm_and_publish_completed_event() {
        SessionContext context = new SessionContext();

        TtsClient ttsClient = mock(TtsClient.class);
        LlmClient llmClient = mock(LlmClient.class);

        when(llmClient.generateReply(eq("corr-1"), anyList()))
            .thenReturn(new LlmGenerationResult(
                true,
                "Can you say that again?",
                "tai-llama",
                1,
                2,
                100L,
                null,
                null
            ));

        ClarificationRequestedEventHandler handler = new ClarificationRequestedEventHandler(
            fixedSessionStore(context),
            eventPublisher,
            ttsClient,
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

        LlmResponseCompletedEvent published =
            eventPublisher.assertSingleEventPublished(LlmResponseCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.responseText()).isEqualTo("Can you say that again?");
    }

    @Test
    void should_publish_failed_event_when_clarification_llm_generation_fails() {
        SessionContext context = new SessionContext();

        TtsClient ttsClient = mock(TtsClient.class);
        LlmClient llmClient = mock(LlmClient.class);

        when(llmClient.generateReply(eq("corr-1"), anyList()))
            .thenReturn(new LlmGenerationResult(
                false,
                null,
                "tai-llama",
                null,
                null,
                100L,
                "LLM_ERROR",
                "LLM failed"
            ));

        ClarificationRequestedEventHandler handler = new ClarificationRequestedEventHandler(
            fixedSessionStore(context),
            eventPublisher,
            ttsClient,
            llmClient
        );

        handler.handle(new ClarificationRequestedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.ORCHESTRATOR
        ));

        LlmResponseFailedEvent published =
            eventPublisher.assertSingleEventPublished(LlmResponseFailedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.errorCode()).isEqualTo("LLM_ERROR");
    }
}
