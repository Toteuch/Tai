// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import java.time.Instant;

public record UiStateRefreshRequestedEvent(
        UiStateRefreshReason reason, String correlationId, Instant requestedAt) {}
