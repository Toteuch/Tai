package com.toteuch.tai.stt.listener.listener;

import com.toteuch.tai.stt.listener.pipeline.SttPipelineSummary;

import java.time.Instant;

public record ListeningRuntimeStatus(
    boolean running,
    ListeningState state,
    String activeCorrelationId,
    Instant lastSegmentAt,
    SttPipelineSummary lastResult,
    String lastError
) {
}
