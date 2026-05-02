// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.runtime;

import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.time.Instant;
import java.util.Map;

public record ModuleRuntimeSnapshot(
        TaiModule module,
        ModuleHealth health,
        ModuleActivity lastActivity,
        Instant lastActivityAt,
        Instant lastHealthAt,
        String lastActiveCorrelationId,
        String lastError,
        Map<String, Object> details) {
    @Override
    public Map<String, Object> details() {
        return details != null ? details : Map.of();
    }
}
