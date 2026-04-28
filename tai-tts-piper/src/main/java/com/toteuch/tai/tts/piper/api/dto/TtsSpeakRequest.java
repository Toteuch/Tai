// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TtsSpeakRequest(@NotBlank String correlationId, @NotBlank String text) {}
