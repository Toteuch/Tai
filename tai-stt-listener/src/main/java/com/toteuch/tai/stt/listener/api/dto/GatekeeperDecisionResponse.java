package com.toteuch.tai.stt.listener.api.dto;

import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;

public record GatekeeperDecisionResponse(
        boolean accepted, String reason, int suspicionScore, String rejectionCategory) {
    public static GatekeeperDecisionResponse from(GatekeeperDecision decision) {
        if (decision == null) {
            return null;
        }
        return new GatekeeperDecisionResponse(
                decision.accepted(),
                decision.reason(),
                decision.suspicionScore(),
                decision.rejectionCategory().name());
    }
}
