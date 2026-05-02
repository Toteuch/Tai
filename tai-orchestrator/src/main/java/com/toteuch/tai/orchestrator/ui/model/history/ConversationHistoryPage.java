// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model.history;

import java.time.Instant;
import java.util.List;

public record ConversationHistoryPage(
        Instant generatedAt,
        Integer limit,
        String nextCursor,
        List<ConversationHistoryItem> items) {}
