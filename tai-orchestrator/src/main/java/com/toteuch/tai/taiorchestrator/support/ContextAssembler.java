package com.toteuch.tai.taiorchestrator.support;

import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.state.AssistantState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContextAssembler {

    private final SystemPromptLoader systemPromptLoader;

    public ContextAssembler(SystemPromptLoader systemPromptLoader) {
        this.systemPromptLoader = systemPromptLoader;
    }

    public List<LlmMessage> assemble(SessionContext sessionContext, AssistantState state, String userText) {
        List<LlmMessage> messages = new ArrayList<>();

        messages.add(new LlmMessage("system", systemPromptLoader.getSystemPrompt()));

        messages.add(new LlmMessage(
            "system",
            "Runtime state: ttsEnabled=" + state.isTtsEnabled()
                + ", obscenityFilterEnabled=" + state.isObscenityFilterEnabled()
        ));

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
                messages.add(new LlmMessage(
                    "system",
                    "The user's previous message was superseded by a newer one before you answered."
                ));
            }

            if (turn.getAssistantMessage() != null && !turn.getAssistantMessage().isBlank()) {
                messages.add(new LlmMessage("assistant", turn.getAssistantMessage()));
            }

            if (turn.isAssistantPlaybackInterrupted()) {
                messages.add(new LlmMessage(
                    "system",
                    "Your previous spoken reply was interrupted by the user before playback completed."
                ));
            }
        }

        messages.add(new LlmMessage("user", userText));
        return messages;
    }
}
