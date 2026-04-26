package com.toteuch.tai.tts.piper.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TtsStopRequest(@NotBlank String correlationId) {}
