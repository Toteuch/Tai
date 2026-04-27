package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConversationTurnCompletedEventHandler
        implements EventHandler<ConversationTurnCompletedEvent> {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");

    private final SessionStore sessionStore;

    public ConversationTurnCompletedEventHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public EventType supports() {
        return EventType.CONVERSATION_TURN_COMPLETED;
    }

    @Override
    public void handle(ConversationTurnCompletedEvent event) {
        SessionContext sessionContext = sessionStore.get();
        if (sessionContext.getThinkingState() != ThinkingState.IDLE) {
            sessionContext.setThinkingState(ThinkingState.IDLE);
            errorLog.error(
                    "ThinkingState should be IDLE | thinkingState={}",
                    sessionContext.getThinkingState());
        }
        if (sessionContext.getSpeakingState() != SpeakingState.SILENT) {
            sessionContext.setSpeakingState(SpeakingState.SILENT);
            errorLog.error(
                    "SpeakingState should be SILENT | speakingState={}",
                    sessionContext.getSpeakingState());
        }

        sessionContext.logMetrics(event.correlationId());

        ConversationTurn activeTurn = sessionContext.getActiveTurn();

        if (activeTurn != null && activeTurn.isPersistInHistory()) {
            sessionContext.addTurn(activeTurn);
        }

        sessionContext.setActiveTurn(null);
    }
}
