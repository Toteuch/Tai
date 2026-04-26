package com.toteuch.tai.stt.listener.api.dto;

public record CaptureDebugResponse(
        boolean success,
        String correlationId,
        SpeechSegmentResponse segment,
        GatekeeperDecisionResponse preGatekeeperDecision,
        TranscriptionResponse transcription,
        GatekeeperDecisionResponse finalGatekeeperDecision) {}
