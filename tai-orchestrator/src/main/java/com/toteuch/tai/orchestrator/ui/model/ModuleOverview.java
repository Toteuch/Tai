// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model;

import java.time.Instant;

public record ModuleOverview(
        ModuleHealth health, String state, Instant lastUpdateAt, boolean stale) {}
