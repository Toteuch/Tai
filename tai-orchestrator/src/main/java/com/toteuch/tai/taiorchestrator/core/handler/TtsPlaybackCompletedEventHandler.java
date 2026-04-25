package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackCompletedEvent;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.state.ListeningState;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import org.springframework.stereotype.Component;

@Component
public class TtsPlaybackCompletedEventHandler implements EventHandler<TtsPlaybackCompletedEvent> {

    private final StateStore stateStore;
    private final SessionStore sessionStore;
    private final UiClient uiClient;

    public TtsPlaybackCompletedEventHandler(
        StateStore stateStore,
        SessionStore sessionStore,
        UiClient uiClient
    ) {
        this.stateStore = stateStore;
        this.sessionStore = sessionStore;
        this.uiClient = uiClient;
    }

    @Override
    public EventType supports() {
        return EventType.TTS_PLAYBACK_COMPLETED;
    }

    @Override
    public void handle(TtsPlaybackCompletedEvent event) {
        SessionContext sessionContext = sessionStore.getOrCreate(event.sessionId());
        ConversationTurn activeTurn = sessionContext.getActiveTurn();

        if (activeTurn != null && event.correlationId().equals(activeTurn.getCorrelationId())) {
            activeTurn.setAssistantPlaybackCompleted(true);
        }

        stateStore.get().setSpeakingState(SpeakingState.SILENT);
        stateStore.get().setListeningState(ListeningState.IDLE);

        uiClient.updateAssistantState(event.sessionId(), stateStore.get());
    }
}
