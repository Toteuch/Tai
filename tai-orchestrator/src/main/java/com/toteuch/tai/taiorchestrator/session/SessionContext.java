package com.toteuch.tai.taiorchestrator.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionContext {

    private final String sessionId;
    private final List<ConversationTurn> turns = new ArrayList<>();
    private TurnExecution currentExecution;
    private ConversationTurn activeTurn;

    public SessionContext(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<ConversationTurn> getTurns() {
        return turns;
    }

    public TurnExecution getCurrentExecution() {
        return currentExecution;
    }

    public void setCurrentExecution(TurnExecution currentExecution) {
        this.currentExecution = currentExecution;
    }

    public ConversationTurn getActiveTurn() {
        return activeTurn;
    }

    public void setActiveTurn(ConversationTurn activeTurn) {
        this.activeTurn = activeTurn;
    }

    public void addTurn(ConversationTurn turn) {
        this.turns.add(turn);
    }

    public Optional<ConversationTurn> findTurnByCorrelationId(String correlationId) {
        return turns.stream()
            .filter(turn -> correlationId.equals(turn.getCorrelationId()))
            .findFirst();
    }
}
