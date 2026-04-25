package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SpeakingState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantSpeechStartedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_mark_assistant_speech_started() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setSpeakingState(SpeakingState.PREPARING);

        AssistantSpeechStartedEventHandler handler =
            new AssistantSpeechStartedEventHandler(fixedSessionStore(context));

        handler.handle(new AssistantSpeechStartedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.TTS_SERVICE
        ));

        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SPEAKING);
        assertThat(turn.isAssistantPlaybackStarted()).isTrue();
    }
}
