// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.internal.AssistantStopSpeakReceivedEvent;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import com.toteuch.tai.orchestrator.session.TurnOutcome;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshReason;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshRequester;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class AssistantStopSpeakReceivedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_not_stop_assistant_speech_if_assistant_is_silent() {
        SessionContext context = new SessionContext();

        TtsClient ttsClient = mock(TtsClient.class);
        UiStateRefreshRequester uiStateRefreshRequester = mock(UiStateRefreshRequester.class);
        ModuleRuntimeUpdater runtimeUpdater = mock(ModuleRuntimeUpdater.class);

        AssistantStopSpeakReceivedEventHandler handler =
                new AssistantStopSpeakReceivedEventHandler(
                        fixedSessionStore(context),
                        ttsClient,
                        runtimeUpdater,
                        uiStateRefreshRequester);

        handler.handle(
                new AssistantStopSpeakReceivedEvent(
                        UUID.randomUUID().toString(), Instant.now(), "corr-1", EventSource.UI));

        verify(ttsClient, never()).stop(anyString());
        verify(uiStateRefreshRequester)
                .requestRefresh(eq(UiStateRefreshReason.RUNTIME_EVENT), eq("corr-1"));
    }

    @Test
    void should_stop_assistant_speech_if_assistant_is_speaking() {
        SessionContext context = new SessionContext();
        ConversationTurn activeTurn =
                new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true);
        activeTurn.setAssistantMessage("Hello Toteuch");
        activeTurn.setAssistantReplyGenerated(true);
        activeTurn.setAssistantPlaybackStarted(true);
        context.setActiveTurn(activeTurn);
        context.setSpeakingState(SpeakingState.SPEAKING);

        TtsClient ttsClient = mock(TtsClient.class);
        UiStateRefreshRequester uiStateRefreshRequester = mock(UiStateRefreshRequester.class);
        ModuleRuntimeUpdater runtimeUpdater = mock(ModuleRuntimeUpdater.class);

        AssistantStopSpeakReceivedEventHandler handler =
                new AssistantStopSpeakReceivedEventHandler(
                        fixedSessionStore(context),
                        ttsClient,
                        runtimeUpdater,
                        uiStateRefreshRequester);

        handler.handle(
                new AssistantStopSpeakReceivedEvent(
                        UUID.randomUUID().toString(), Instant.now(), "corr-1", EventSource.UI));

        verify(ttsClient).stop(eq("corr-1"));
        assertThat(context.getActiveTurn()).isNull();
        assertThat(context.getTurns().size()).isEqualTo(1);
        assertThat(context.getTurns().getFirst().isAssistantPlaybackInterrupted()).isTrue();
        assertThat(context.getTurns().getFirst().isAssistantPlaybackCompleted()).isFalse();
        assertThat(context.getTurns().getFirst().getCorrelationId()).isEqualTo("corr-1");
        assertThat(context.getTurns().getFirst().getOutcome()).isEqualTo(TurnOutcome.INTERRUPTED);
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        verify(runtimeUpdater).ttsIdle();
        verify(uiStateRefreshRequester)
                .requestRefresh(eq(UiStateRefreshReason.RUNTIME_EVENT), eq("corr-1"));
    }

    @Test
    void should_end_the_turn_if_assistant_is_thinking() {
        SessionContext context = new SessionContext();
        ConversationTurn activeTurn =
                new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true);
        context.setActiveTurn(activeTurn);
        context.setThinkingState(ThinkingState.GENERATING);

        TtsClient ttsClient = mock(TtsClient.class);
        UiStateRefreshRequester uiStateRefreshRequester = mock(UiStateRefreshRequester.class);
        ModuleRuntimeUpdater runtimeUpdater = mock(ModuleRuntimeUpdater.class);

        AssistantStopSpeakReceivedEventHandler handler =
                new AssistantStopSpeakReceivedEventHandler(
                        fixedSessionStore(context),
                        ttsClient,
                        runtimeUpdater,
                        uiStateRefreshRequester);

        handler.handle(
                new AssistantStopSpeakReceivedEvent(
                        UUID.randomUUID().toString(), Instant.now(), "corr-1", EventSource.UI));

        verify(ttsClient, never()).stop(anyString());
        assertThat(context.getActiveTurn()).isNull();
        assertThat(context.getTurns().size()).isEqualTo(1);
        assertThat(context.getTurns().getFirst().isAssistantPlaybackInterrupted()).isFalse();
        assertThat(context.getTurns().getFirst().isAssistantPlaybackCompleted()).isFalse();
        assertThat(context.getTurns().getFirst().getCorrelationId()).isEqualTo("corr-1");
        assertThat(context.getTurns().getFirst().getOutcome()).isEqualTo(TurnOutcome.INTERRUPTED);
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        verify(runtimeUpdater).llmIdle();
        verify(uiStateRefreshRequester)
                .requestRefresh(eq(UiStateRefreshReason.RUNTIME_EVENT), eq("corr-1"));
    }
}
