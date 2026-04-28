package com.toteuch.tai.orchestrator.transport.debug.dto;

import java.time.Instant;
import java.util.Map;

public record SystemHealthResponse(
        String status, Instant checkedAt, Map<String, ServiceHealthResponse> services) {}
