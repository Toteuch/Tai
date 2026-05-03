// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechCompletedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshRequester;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssistantSpeechCompletedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_mark_speech_completed_and_publish_turn_completed() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setSpeakingState(SpeakingState.SPEAKING);

        ModuleRuntimeUpdater runtimeUpdater = mock(ModuleRuntimeUpdater.class);
        UiStateRefreshRequester uiStateRefreshRequester = mock(UiStateRefreshRequester.class);

        AssistantSpeechCompletedEventHandler handler =
                new AssistantSpeechCompletedEventHandler(
                        fixedSessionStore(context),
                        eventPublisher,
                        runtimeUpdater,
                        uiStateRefreshRequester);

        handler.handle(
                new AssistantSpeechCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.TTS_SERVICE,
                        0L));

        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(turn.isAssistantPlaybackCompleted()).isTrue();

        ConversationTurnCompletedEvent published =
                eventPublisher.assertSingleEventPublished(ConversationTurnCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
    }
}
