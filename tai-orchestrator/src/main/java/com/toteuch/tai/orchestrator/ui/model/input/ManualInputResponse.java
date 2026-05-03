// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model.input;

import java.time.Instant;

public record ManualInputResponse(boolean accepted, String correlationId, Instant acceptedAt) {}
