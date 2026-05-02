// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import java.time.Instant;
import java.util.Map;

public record ModuleHealthResult(
        TaiModule module,
        String status,
        Instant respondedAt,
        String error,
        Map<String, Object> details,
        ModuleActivity activity) {

    public static ModuleHealthResult success(
            TaiModule module,
            String status,
            Instant respondedAt,
            Map<String, Object> details,
            ModuleActivity activity) {
        return new ModuleHealthResult(
                module, status, respondedAt, null, details != null ? details : Map.of(), activity);
    }

    public static ModuleHealthResult failure(
            TaiModule module, String status, Instant respondedAt, String error) {
        return new ModuleHealthResult(module, status, respondedAt, error, Map.of(), null);
    }
}
