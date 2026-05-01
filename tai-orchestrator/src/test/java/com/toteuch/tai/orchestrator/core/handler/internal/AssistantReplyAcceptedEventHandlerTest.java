// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyAcceptedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssistantReplyAcceptedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_attach_reply_and_call_tts_when_tts_enabled() {
        SessionContext context = new SessionContext();
        context.setActiveTurn(new ConversationTurn("corr-1", "Hello", Instant.now(), true));
        context.setThinkingState(ThinkingState.GENERATING);

        TtsClient ttsClient = mock(TtsClient.class);

        AssistantReplyAcceptedEventHandler handler =
                new AssistantReplyAcceptedEventHandler(
                        fixedSessionStore(context), eventPublisher, ttsClient);

        handler.handle(
                new AssistantReplyAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.LLM_SERVICE,
                        "Hello   Tai!!!",
                        200L));

        assertThat(context.getActiveTurn().getAssistantMessage()).isEqualTo("Hello Tai!");
        assertThat(context.getActiveTurn().isAssistantReplyGenerated()).isTrue();
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.PREPARING);

        verify(ttsClient).speak("corr-1", "Hello Tai!");
        eventPublisher.assertNoEventPublished();
    }

    @Test
    void should_complete_turn_when_tts_disabled() {
        SessionContext context = new SessionContext();
        context.setTtsEnabled(false);
        context.setActiveTurn(new ConversationTurn("corr-1", "Hello", Instant.now(), true));

        TtsClient ttsClient = mock(TtsClient.class);

        AssistantReplyAcceptedEventHandler handler =
                new AssistantReplyAcceptedEventHandler(
                        fixedSessionStore(context), eventPublisher, ttsClient);

        handler.handle(
                new AssistantReplyAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.LLM_SERVICE,
                        "Hi",
                        200L));

        verifyNoInteractions(ttsClient);

        ConversationTurnCompletedEvent published =
                eventPublisher.assertSingleEventPublished(ConversationTurnCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
    }
}
