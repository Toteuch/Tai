package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackFailedEvent;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import org.springframework.stereotype.Component;

@Component
public class TtsPlaybackFailedEventHandler implements EventHandler<TtsPlaybackFailedEvent> {

    private final StateStore stateStore;
    private final SessionStore sessionStore;
    private final UiClient uiClient;

    public TtsPlaybackFailedEventHandler(
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
        return EventType.TTS_PLAYBACK_FAILED;
    }

    @Override
    public void handle(TtsPlaybackFailedEvent event) {
        SessionContext sessionContext = sessionStore.getOrCreate(event.sessionId());
        ConversationTurn activeTurn = sessionContext.getActiveTurn();

        if (activeTurn != null && event.correlationId().equals(activeTurn.getCorrelationId())) {
            activeTurn.setAssistantPlaybackInterrupted(true);
        }

        stateStore.get().setSpeakingState(SpeakingState.SILENT);

        uiClient.showError(event.sessionId(), event.errorMessage());
        uiClient.updateAssistantState(event.sessionId(), stateStore.get());
    }
}
