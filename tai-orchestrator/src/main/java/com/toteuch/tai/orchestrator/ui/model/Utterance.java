// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.model;

import java.time.Instant;

public record Utterance(
        UtteranceRole role,
        String text,
        Instant startedAt,
        Instant updatedAt,
        String correlationId,
        UtteranceStatus status) {}
