// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model;

import java.time.Instant;
import java.util.Map;

public record TaiUiState(
        String schemaVersion,
        long sequence,
        Instant generatedAt,
        ConversationStatus conversationStatus,
        Map<TaiModule, ModuleOverview> modules,
        Utterance lastUserUtterance,
        Utterance lastAssistantUtterance) {}
