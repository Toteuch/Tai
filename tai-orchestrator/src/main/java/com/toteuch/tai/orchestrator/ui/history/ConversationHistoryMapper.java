// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.history;

import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryItem;
import org.springframework.stereotype.Component;

@Component
public class ConversationHistoryMapper {

    public ConversationHistoryItem toHistoryItem(ConversationTurn turn) {
        return new ConversationHistoryItem(
                turn.getCorrelationId(),
                turn.getUserMessage(),
                turn.getAssistantMessage(),
                turn.getOutcome(),
                turn.getCreatedAt(),
                turn.getCompletedAt());
    }
}
