package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackCompletedEvent;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.InMemorySessionStore;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.state.InMemoryStateStore;
import com.toteuch.tai.taiorchestrator.state.ListeningState;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TtsPlaybackCompletedEventHandlerTest {

    private StateStore stateStore;
    private SessionStore sessionStore;
    private UiClient uiClient;
    private TtsPlaybackCompletedEventHandler handler;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
        sessionStore = new InMemorySessionStore();
        uiClient = mock(UiClient.class);
        handler = new TtsPlaybackCompletedEventHandler(stateStore, sessionStore, uiClient);
    }

    @Test
    void shouldMarkPlaybackCompletedAndResetStates() {
        SessionContext sessionContext = sessionStore.getOrCreate("session-1");
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now());
        turn.setAssistantMessage("Hi");
        turn.setAssistantReplyGenerated(true);
        turn.setAssistantPlaybackStarted(true);
        sessionContext.addTurn(turn);
        sessionContext.setActiveTurn(turn);

        stateStore.get().setSpeakingState(SpeakingState.SPEAKING);

        TtsPlaybackCompletedEvent event = new TtsPlaybackCompletedEvent(
            "event-1",
            Instant.now(),
            "session-1",
            "corr-1",
            EventSource.TTS_SERVICE,
            "Hi",
            1200L
        );

        handler.handle(event);

        assertTrue(turn.isAssistantPlaybackCompleted());
        assertEquals(SpeakingState.SILENT, stateStore.get().getSpeakingState());
        assertEquals(ListeningState.IDLE, stateStore.get().getListeningState());
        verify(uiClient).updateAssistantState(eq("session-1"), any());
    }
}
