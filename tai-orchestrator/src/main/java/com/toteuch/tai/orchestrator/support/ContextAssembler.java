package com.toteuch.tai.orchestrator.support;

import com.toteuch.tai.orchestrator.services.llm.LlmMessage;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContextAssembler {

    private final SystemPromptLoader systemPromptLoader;

    public ContextAssembler(SystemPromptLoader systemPromptLoader) {
        this.systemPromptLoader = systemPromptLoader;
    }

    public static List<LlmMessage> assemble(SessionContext sessionContext, boolean forLog) {
        List<LlmMessage> messages = new ArrayList<>();

        int fromIndex = Math.max(0, sessionContext.getTurns().size() - 6);
        List<ConversationTurn> recentTurns = sessionContext.getTurns().subList(fromIndex, sessionContext.getTurns().size());

        String activeCorrelationId = sessionContext.getActiveTurn() != null
            ? sessionContext.getActiveTurn().getCorrelationId()
            : null;

        for (ConversationTurn turn : recentTurns) {
            boolean isCurrentActiveTurn = activeCorrelationId != null
                && activeCorrelationId.equals(turn.getCorrelationId());

            if (isCurrentActiveTurn) {
                continue;
            }

            if (turn.getUserMessage() != null && !turn.getUserMessage().isBlank()) {
                messages.add(new LlmMessage("user", turn.getUserMessage()));
            }

            if (turn.isSupersededBeforeAssistantReply()) {
                if (forLog) {
                    messages.add(new LlmMessage(
                        "system",
                        "PREVIOUS_USER_MESSAGE_SUPERSEDED"
                    ));
                } else {
                    messages.add(new LlmMessage(
                        "system",
                        "The user's previous message was superseded by a newer one before you answered."
                    ));
                }
            }

            if (turn.getAssistantMessage() != null && !turn.getAssistantMessage().isBlank()) {
                messages.add(new LlmMessage("assistant", turn.getAssistantMessage()));
            }

            if (turn.isAssistantPlaybackInterrupted()) {
                if (forLog) {
                    messages.add(new LlmMessage(
                        "system",
                        "ASSISTANT_PLAYBACK_INTERRUPTED"
                    ));
                } else {
                    messages.add(new LlmMessage(
                        "system",
                        "Your previous spoken reply was interrupted by the user before playback completed."
                    ));
                }
            }
        }
        return messages;
    }

    public List<LlmMessage> assemble(SessionContext sessionContext, String userText, boolean forLog) {
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPromptLoader.getSystemPrompt()));
        messages.addAll(assemble(sessionContext, forLog));
        messages.add(new LlmMessage("user", userText));
        return messages;
    }
}
