// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.gatekeeper;

public record GatekeeperDecision(
        boolean accepted, String reason, int suspicionScore, RejectionCategory rejectionCategory) {
    public static GatekeeperDecision accepted(int suspicionScore) {
        return new GatekeeperDecision(true, "ACCEPTED", suspicionScore, RejectionCategory.NONE);
    }

    public static GatekeeperDecision noise(String reason) {
        return new GatekeeperDecision(false, reason, 999, RejectionCategory.NOISE);
    }

    public static GatekeeperDecision unintelligible(String reason) {
        return new GatekeeperDecision(false, reason, 999, RejectionCategory.UNINTELLIGIBLE);
    }

    public static GatekeeperDecision suspicious(String reason, int suspicionScore) {
        return new GatekeeperDecision(
                false, reason, suspicionScore, RejectionCategory.UNINTELLIGIBLE);
    }
}
