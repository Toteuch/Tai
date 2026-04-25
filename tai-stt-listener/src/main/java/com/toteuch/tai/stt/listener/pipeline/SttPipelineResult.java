package com.toteuch.tai.stt.listener.pipeline;

import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;

import java.time.Instant;

public record SttPipelineResult(
    String correlationId,
    SpeechSegment segment,
    GatekeeperDecision preDecision,
    TranscriptionResult transcription,
    GatekeeperDecision finalDecision,
    Instant completedAt
) {
    public boolean accepted() {
        return finalDecision != null && finalDecision.accepted();
    }
}
