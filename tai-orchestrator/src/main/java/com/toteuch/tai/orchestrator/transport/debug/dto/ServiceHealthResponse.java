package com.toteuch.tai.orchestrator.transport.debug.dto;

public record ServiceHealthResponse(
        String status, String url, Long responseTimeMs, String errorMessage) {}
