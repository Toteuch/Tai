package com.toteuch.tai.orchestrator.session;

import com.toteuch.tai.orchestrator.services.llm.LlmMessage;
import com.toteuch.tai.orchestrator.support.ContextAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SessionContext {
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");
    private static final Logger convLog = LoggerFactory.getLogger("tai.conversation");

    private final List<ConversationTurn> turns = new ArrayList<>();
    private ThinkingState thinkingState = ThinkingState.IDLE;
    private SpeakingState speakingState = SpeakingState.SILENT;
    private boolean ttsEnabled = true;
    private boolean obscenityFilterEnabled = false;
    private ConversationTurn activeTurn;

    public List<ConversationTurn> getTurns() {
        return turns;
    }

    public ConversationTurn getActiveTurn() {
        return activeTurn;
    }

    public void setActiveTurn(ConversationTurn activeTurn) {
        this.activeTurn = activeTurn;
    }

    public void addTurn(ConversationTurn turn) {
        contextLog.info("Adding turn | correlationId={}", turn.getCorrelationId());
        logConversation(turn);
        this.turns.add(turn);
    }

    public ThinkingState getThinkingState() {
        return thinkingState;
    }

    public void setThinkingState(ThinkingState thinkingState) {
        if (this.thinkingState != thinkingState) {
            contextLog.debug("ThinkingState changed | newState={} oldState={}",
                thinkingState,
                this.thinkingState
            );
        } else {
            contextLog.error("ThinkingState changed | newState={} oldState={}",
                thinkingState,
                this.thinkingState
            );
        }
        this.thinkingState = thinkingState;
    }

    public SpeakingState getSpeakingState() {
        return speakingState;
    }

    public void setSpeakingState(SpeakingState speakingState) {
        if (this.speakingState != speakingState) {
            contextLog.debug("SpeakingState changed | newState={} oldState={}",
                speakingState,
                this.speakingState
            );
        } else {
            contextLog.error("SpeakingState changed | newState={} oldState={}",
                speakingState,
                this.speakingState
            );
        }

        this.speakingState = speakingState;
    }

    public boolean isTtsEnabled() {
        return ttsEnabled;
    }

    public void setTtsEnabled(boolean ttsEnabled) {
        contextLog.info("TtsEnabled changed to {}", ttsEnabled);
        this.ttsEnabled = ttsEnabled;
    }

    public boolean isObscenityFilterEnabled() {
        return obscenityFilterEnabled;
    }

    public void setObscenityFilterEnabled(boolean obscenityFilterEnabled) {
        contextLog.info("ObscenityFilterEnabled changed to {}", obscenityFilterEnabled);
        this.obscenityFilterEnabled = obscenityFilterEnabled;
    }

    public boolean isStillActiveTurn(String correlationId) {
        try {
            return activeTurn.getCorrelationId().equals(correlationId);
        } catch (NullPointerException npe) {
            // No active turn
            return false;
        }
    }

    private void logConversation(ConversationTurn turn) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("""
            
            === TAI TURN START ===
            """);
        logBuilder.append(String.format("""
            correlationId=%s
            """, turn.getCorrelationId()));
        logBuilder.append(String.format("""
            history=%s
            """, formatHistoryOverview()));
        if (convLog.isDebugEnabled()) {
            logBuilder.append(String.format("""
                    historyDetails=
                    ---------------
                %s
                    ---------------
                """, formatFullHistory()));
        }
        logBuilder.append(String.format("""
            userMessage=%s
            """, turn.getUserMessage()));
        logBuilder.append(String.format("""
                assistantReply=%s
                """,
            turn.getAssistantMessage() != null ? turn.getAssistantMessage() : "<empty>"));
        logBuilder.append("""
            === TAI TURN END ===""");
        convLog.info(logBuilder.toString());
    }

    private String formatHistoryOverview() {
        StringBuilder historyOverview = new StringBuilder("[");
        List<LlmMessage> messages = ContextAssembler.assemble(this, true);
        historyOverview.append(String.format("%s messages=", messages.size()));
        messages.forEach(m -> historyOverview.append(m.role()).append(", "));
        return historyOverview.lastIndexOf(", ") > -1
            ? historyOverview.subSequence(0, historyOverview.lastIndexOf(", ")).toString() + "]"
            : historyOverview + "]";
    }

    private String formatFullHistory() {
        StringBuilder fullHistory = new StringBuilder();
        List<LlmMessage> messages = ContextAssembler.assemble(this, true);
        messages.forEach(m -> {
            fullHistory.append("\t").append(m.role()).append(": ").append(m.content()).append("\n");
        });
        return fullHistory.toString();
    }
}
