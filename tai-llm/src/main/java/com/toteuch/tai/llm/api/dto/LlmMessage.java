package com.toteuch.tai.llm.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LlmMessage(@NotBlank String role, @NotBlank String content) {
}
