// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.internal.UserSpeechStartedEvent;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UserSpeechStartedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_do_nothing_if_not_barged_in() {
        SessionContext context = new SessionContext();

        TtsClient ttsClient = mock(TtsClient.class);

        UserSpeechStartedEventHandler handler =
                new UserSpeechStartedEventHandler(fixedSessionStore(context), ttsClient);

        handler.handle(
                new UserSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.ORCHESTRATOR));

        verify(ttsClient, never()).stop(anyString());
    }

    @Test
    void should_stop_tts_if_barged_in() {
        SessionContext context = new SessionContext();
        ConversationTurn activeTurn =
                new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true);
        activeTurn.setAssistantMessage("Hello Toteuch");
        activeTurn.setAssistantReplyGenerated(true);
        activeTurn.setAssistantPlaybackStarted(true);
        context.setActiveTurn(activeTurn);
        context.setSpeakingState(SpeakingState.SPEAKING);

        TtsClient ttsClient = mock(TtsClient.class);

        UserSpeechStartedEventHandler handler =
                new UserSpeechStartedEventHandler(fixedSessionStore(context), ttsClient);

        handler.handle(
                new UserSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.ORCHESTRATOR));

        verify(ttsClient).stop(eq("corr-1"));
        assertThat(context.getActiveTurn()).isNull();
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(context.getTurns().size()).isEqualTo(1);
        ConversationTurn historizedTurn = context.getTurns().getFirst();
        assertThat(historizedTurn.getCorrelationId()).isEqualTo("corr-1");
        assertThat(historizedTurn.isAssistantPlaybackCompleted()).isEqualTo(false);
        assertThat(historizedTurn.isAssistantPlaybackStarted()).isEqualTo(true);
        assertThat(historizedTurn.isAssistantPlaybackInterrupted()).isEqualTo(true);
        assertThat(historizedTurn.isAssistantReplyGenerated()).isEqualTo(true);
        assertThat(historizedTurn.isSupersededBeforeAssistantReply()).isEqualTo(false);
    }

    @Test
    void should_stop_preparing_tts_if_barged_in() {
        SessionContext context = new SessionContext();
        ConversationTurn activeTurn =
                new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true);
        activeTurn.setAssistantMessage("Hello Toteuch");
        activeTurn.setAssistantReplyGenerated(true);
        context.setActiveTurn(activeTurn);
        context.setSpeakingState(SpeakingState.PREPARING);

        TtsClient ttsClient = mock(TtsClient.class);

        UserSpeechStartedEventHandler handler =
                new UserSpeechStartedEventHandler(fixedSessionStore(context), ttsClient);

        handler.handle(
                new UserSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.ORCHESTRATOR));

        verify(ttsClient).stop(eq("corr-1"));
        assertThat(context.getActiveTurn()).isNull();
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(context.getTurns().size()).isEqualTo(1);
        ConversationTurn historizedTurn = context.getTurns().getFirst();
        assertThat(historizedTurn.getCorrelationId()).isEqualTo("corr-1");
        assertThat(historizedTurn.isAssistantPlaybackCompleted()).isEqualTo(false);
        assertThat(historizedTurn.isAssistantPlaybackStarted()).isEqualTo(false);
        assertThat(historizedTurn.isAssistantPlaybackInterrupted()).isEqualTo(true);
        assertThat(historizedTurn.isAssistantReplyGenerated()).isEqualTo(true);
        assertThat(historizedTurn.isSupersededBeforeAssistantReply()).isEqualTo(false);
    }

    @Test
    void should_stop_generating_llm_if_barged_in() {
        SessionContext context = new SessionContext();

        context.setActiveTurn(new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true));
        context.setThinkingState(ThinkingState.GENERATING);

        TtsClient ttsClient = mock(TtsClient.class);

        UserSpeechStartedEventHandler handler =
                new UserSpeechStartedEventHandler(fixedSessionStore(context), ttsClient);

        handler.handle(
                new UserSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.ORCHESTRATOR));

        verify(ttsClient, never()).stop(anyString());
        assertThat(context.getActiveTurn()).isNull();
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(context.getTurns().size()).isEqualTo(1);
        ConversationTurn historizedTurn = context.getTurns().getFirst();
        assertThat(historizedTurn.getCorrelationId()).isEqualTo("corr-1");
        assertThat(historizedTurn.isAssistantPlaybackCompleted()).isEqualTo(false);
        assertThat(historizedTurn.isAssistantPlaybackStarted()).isEqualTo(false);
        assertThat(historizedTurn.isAssistantPlaybackInterrupted()).isEqualTo(false);
        assertThat(historizedTurn.isAssistantReplyGenerated()).isEqualTo(false);
        assertThat(historizedTurn.isSupersededBeforeAssistantReply()).isEqualTo(true);
        assertThat(historizedTurn.getSupersededByCorrelationId()).isEqualTo("corr-2");
    }
}
