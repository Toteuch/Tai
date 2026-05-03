// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model;

import java.time.Instant;
import java.util.Map;

public record ModuleDetails(
        TaiModule module,
        ModuleHealth health,
        String state,
        Instant checkedAt,
        String lastCorrelationId,
        Long lastProcessTimeMs,
        String lastError,
        Map<String, Object> details) {}
