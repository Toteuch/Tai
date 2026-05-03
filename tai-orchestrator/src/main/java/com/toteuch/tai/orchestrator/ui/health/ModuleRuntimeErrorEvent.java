// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.time.Instant;

public record ModuleRuntimeErrorEvent(
        TaiModule module, String lastActiveCorrelationId, Instant occurredAt) {}
