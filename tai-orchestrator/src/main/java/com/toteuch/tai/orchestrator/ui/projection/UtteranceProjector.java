// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.model.Utterance;
import com.toteuch.tai.orchestrator.ui.model.UtteranceRole;
import com.toteuch.tai.orchestrator.ui.model.UtteranceStatus;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UtteranceProjector {

    private final ModuleRuntimeRegistry registry;

    public UtteranceProjector(ModuleRuntimeRegistry registry) {
        this.registry = registry;
    }

    public Utterance projectLastUserUtterance(SessionContext sessionContext) {
        ConversationTurn turn = latestTurnWithUserText(sessionContext);

        if (turn == null) {
            return null;
        }

        return new Utterance(
                UtteranceRole.USER,
                turn.getUserMessage(),
                turn.getCreatedAt(),
                updatedAt(turn),
                turn.getCorrelationId(),
                UtteranceStatus.COMPLETED);
    }

    public Utterance projectLastAssistantUtterance(SessionContext sessionContext) {
        ConversationTurn turn = latestTurnWithAssistantState(sessionContext);

        if (turn == null) {
            return null;
        }

        return new Utterance(
                UtteranceRole.ASSISTANT,
                turn.getAssistantMessage(),
                turn.getCreatedAt(),
                updatedAt(turn),
                turn.getCorrelationId(),
                assistantStatus(turn, isActiveTurn(sessionContext, turn)));
    }

    private ConversationTurn latestTurnWithUserText(SessionContext sessionContext) {
        ConversationTurn activeTurn = sessionContext.getActiveTurn();

        if (hasText(activeTurn != null ? activeTurn.getUserMessage() : null)) {
            return activeTurn;
        }

        return latestCompletedTurnMatching(
                sessionContext.getTurns(), turn -> hasText(turn.getUserMessage()));
    }

    private ConversationTurn latestTurnWithAssistantState(SessionContext sessionContext) {
        ConversationTurn activeTurn = sessionContext.getActiveTurn();

        if (activeTurn != null
                && (hasText(activeTurn.getAssistantMessage()) || isFailed(activeTurn))) {
            return activeTurn;
        }

        return latestCompletedTurnMatching(
                sessionContext.getTurns(),
                turn -> hasText(turn.getAssistantMessage()) || isCompleted(turn));
    }

    private ConversationTurn latestCompletedTurnMatching(
            List<ConversationTurn> turns,
            java.util.function.Predicate<ConversationTurn> predicate) {
        for (int index = turns.size() - 1; index >= 0; index--) {
            ConversationTurn turn = turns.get(index);

            if (predicate.test(turn)) {
                return turn;
            }
        }

        return null;
    }

    private boolean isActiveTurn(SessionContext sessionContext, ConversationTurn turn) {
        return sessionContext.getActiveTurn() == turn;
    }

    private UtteranceStatus assistantStatus(ConversationTurn turn, boolean active) {
        if (active) {
            ModuleActivity ttsActivity = activityOf(TaiModule.TTS_PIPER);
            ModuleActivity llmActivity = activityOf(TaiModule.LLM);

            if (ttsActivity == ModuleActivity.ERROR || llmActivity == ModuleActivity.ERROR) {
                return UtteranceStatus.FAILED;
            }

            if (ttsActivity == ModuleActivity.SYNTHESIZING
                    || ttsActivity == ModuleActivity.SPEAKING) {
                return UtteranceStatus.STARTED;
            }

            return UtteranceStatus.STARTED;
        }

        if (isFailed(turn)) {
            return UtteranceStatus.FAILED;
        }

        if (isInterrupted(turn) || isSuperseded(turn)) {
            return UtteranceStatus.INTERRUPTED;
        }

        return UtteranceStatus.COMPLETED;
    }

    private boolean isFailed(ConversationTurn turn) {
        return hasOutcome(turn, "FAILED");
    }

    private boolean isInterrupted(ConversationTurn turn) {
        return hasOutcome(turn, "INTERRUPTED");
    }

    private boolean isSuperseded(ConversationTurn turn) {
        return hasOutcome(turn, "SUPERSEDED");
    }

    private boolean isCompleted(ConversationTurn turn) {
        return hasOutcome(turn, "COMPLETED");
    }

    private boolean hasOutcome(ConversationTurn turn, String expectedName) {
        if (turn == null || turn.getOutcome() == null) {
            return false;
        }

        return expectedName.equals(turn.getOutcome().name());
    }

    private ModuleActivity activityOf(TaiModule module) {
        ModuleRuntimeSnapshot snapshot = registry.get(module);
        return snapshot == null ? null : snapshot.lastActivity();
    }

    private Instant updatedAt(ConversationTurn turn) {
        if (turn.getCompletedAt() != null) {
            return turn.getCompletedAt();
        }

        return turn.getCreatedAt();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
