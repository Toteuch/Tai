package com.toteuch.tai.llm.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record LlmGenerationRequest(
        @NotBlank String correlationId, @NotEmpty List<@Valid LlmMessage> messages) {}
