// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssistantSpeechStartedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_mark_assistant_speech_started() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setSpeakingState(SpeakingState.PREPARING);

        AssistantSpeechStartedEventHandler handler =
                new AssistantSpeechStartedEventHandler(fixedSessionStore(context));

        handler.handle(
                new AssistantSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.TTS_SERVICE,
                        "Hello",
                        0L));

        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SPEAKING);
        assertThat(turn.isAssistantPlaybackStarted()).isTrue();
    }
}
