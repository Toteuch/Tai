// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import com.toteuch.tai.orchestrator.services.llm.LlmClient;
import com.toteuch.tai.orchestrator.services.llm.LlmMessage;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import com.toteuch.tai.orchestrator.support.ContextAssembler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserUtteranceAcceptedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_create_active_turn_call_llm_and_publish_completed_event() {
        SessionContext context = new SessionContext();

        LlmClient llmClient = mock(LlmClient.class);
        ContextAssembler contextAssembler = mock(ContextAssembler.class);

        when(contextAssembler.assemble(eq(context), eq("Hello"), eq(false)))
                .thenReturn(List.of(new LlmMessage("user", "Hello")));

        UserUtteranceAcceptedEventHandler handler =
                new UserUtteranceAcceptedEventHandler(
                        fixedSessionStore(context), contextAssembler, llmClient);

        handler.handle(
                new UserUtteranceAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.STT_SERVICE,
                        "Hello",
                        0L));

        assertThat(context.getActiveTurn()).isNotNull();
        assertThat(context.getActiveTurn().getCorrelationId()).isEqualTo("corr-1");
        assertThat(context.getActiveTurn().getUserMessage()).isEqualTo("Hello");
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.GENERATING);

        verify(llmClient).generateReply(eq("corr-1"), anyList());
        publishLlmCompletedEvent("corr-1", "Hi");

        LlmResponseCompletedEvent published =
                eventPublisher.assertSingleEventPublished(LlmResponseCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.responseText()).isEqualTo("Hi");
    }

    @Test
    void should_publish_failed_event_when_llm_generation_fails() {
        SessionContext context = new SessionContext();

        TtsClient ttsClient = mock(TtsClient.class);
        LlmClient llmClient = mock(LlmClient.class);
        ContextAssembler contextAssembler = mock(ContextAssembler.class);

        when(contextAssembler.assemble(eq(context), eq("Hello"), eq(false)))
                .thenReturn(List.of(new LlmMessage("user", "Hello")));

        UserUtteranceAcceptedEventHandler handler =
                new UserUtteranceAcceptedEventHandler(
                        fixedSessionStore(context), contextAssembler, llmClient);

        handler.handle(
                new UserUtteranceAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.STT_SERVICE,
                        "Hello",
                        0L));

        verify(llmClient).generateReply(eq("corr-1"), anyList());
        publishLlmFailedEvent("corr-1");

        LlmResponseFailedEvent published =
                eventPublisher.assertSingleEventPublished(LlmResponseFailedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.errorCode()).isEqualTo("LLM_ERROR");
        assertThat(published.errorMessage()).isEqualTo("LLM failed");
    }
}
