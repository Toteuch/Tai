// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssistantSpeechFailedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_set_speaking_silent_and_publish_turn_completed() {
        SessionContext context = new SessionContext();
        context.setSpeakingState(SpeakingState.SPEAKING);

        AssistantSpeechFailedEventHandler handler =
                new AssistantSpeechFailedEventHandler(fixedSessionStore(context), eventPublisher);

        handler.handle(
                new AssistantSpeechFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.TTS_SERVICE,
                        "TTS_ERROR",
                        "TTS failed",
                        0L));

        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);

        ConversationTurnCompletedEvent published =
                eventPublisher.assertSingleEventPublished(ConversationTurnCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
    }
}
