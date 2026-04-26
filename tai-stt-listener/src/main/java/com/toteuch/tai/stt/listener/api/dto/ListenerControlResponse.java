package com.toteuch.tai.stt.listener.api.dto;

import com.toteuch.tai.stt.listener.listener.ListeningRuntimeStatus;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineSummary;

import java.time.Instant;

public record ListenerControlResponse(
    boolean running,
    String state,
    String activeCorrelationId,
    Instant lastSegmentAt,
    SttPipelineSummary lastResult,
    String lastError
) {
    public static ListenerControlResponse from(ListeningRuntimeStatus status) {
        return new ListenerControlResponse(
            status.running(),
            status.state().name(),
            status.activeCorrelationId(),
            status.lastSegmentAt(),
            status.lastResult(),
            status.lastError()
        );
    }
}
