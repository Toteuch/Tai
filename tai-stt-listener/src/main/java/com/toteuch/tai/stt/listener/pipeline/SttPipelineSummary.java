package com.toteuch.tai.stt.listener.pipeline;

import java.time.Instant;

public record SttPipelineSummary(
    boolean accepted,
    String reason,
    String rejectionCategory,
    String text,
    String language,
    Instant completedAt
) {
    public static SttPipelineSummary from(SttPipelineResult result) {
        if (result == null || result.finalDecision() == null) {
            return null;
        }

        return new SttPipelineSummary(
            result.finalDecision().accepted(),
            result.finalDecision().reason(),
            result.finalDecision().rejectionCategory().name(),
            result.transcription() == null ? null : result.transcription().text(),
            result.transcription() == null ? null : result.transcription().language(),
            result.completedAt()
        );
    }
}
