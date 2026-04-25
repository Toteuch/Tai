package com.toteuch.tai.taiorchestrator.services.stt.audio.gatekeeper;

public record GatekeeperDecision(
    boolean accepted,
    String reason,
    int suspicionScore,
    RejectionCategory rejectionCategory
) {
}
