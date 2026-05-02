// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model.history;

import com.toteuch.tai.orchestrator.session.TurnOutcome;
import java.time.Instant;

public record ConversationHistoryItem(
        String correlationId,
        String userText,
        String assistantText,
        TurnOutcome outcome,
        Instant startedAt,
        Instant completedAt) {}
