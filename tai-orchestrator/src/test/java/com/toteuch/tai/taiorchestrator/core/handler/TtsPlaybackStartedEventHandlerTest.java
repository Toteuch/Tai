package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackStartedEvent;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.InMemorySessionStore;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.state.InMemoryStateStore;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TtsPlaybackStartedEventHandlerTest {

    private StateStore stateStore;
    private SessionStore sessionStore;
    private UiClient uiClient;
    private TtsPlaybackStartedEventHandler handler;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
        sessionStore = new InMemorySessionStore();
        uiClient = mock(UiClient.class);
        handler = new TtsPlaybackStartedEventHandler(stateStore, sessionStore, uiClient);
    }

    @Test
    void shouldMarkActiveTurnPlaybackStarted() {
        SessionContext sessionContext = sessionStore.getOrCreate("session-1");
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now());
        turn.setAssistantMessage("Hi");
        turn.setAssistantReplyGenerated(true);
        sessionContext.addTurn(turn);
        sessionContext.setActiveTurn(turn);

        TtsPlaybackStartedEvent event = new TtsPlaybackStartedEvent(
            "event-1",
            Instant.now(),
            "session-1",
            "corr-1",
            EventSource.TTS_SERVICE,
            "Hi",
            "voice-1"
        );

        handler.handle(event);

        assertEquals(SpeakingState.SPEAKING, stateStore.get().getSpeakingState());
        assertTrue(turn.isAssistantPlaybackStarted());
        verify(uiClient).updateAssistantState(eq("session-1"), any());
    }
}
