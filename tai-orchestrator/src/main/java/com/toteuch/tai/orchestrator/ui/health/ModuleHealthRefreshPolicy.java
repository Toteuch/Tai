// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ModuleHealthRefreshPolicy {

    private final ModuleHealthRefreshProperties properties;
    private final Clock clock;

    public ModuleHealthRefreshPolicy(ModuleHealthRefreshProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public boolean shouldRefresh(
            TaiModule module,
            ModuleRuntimeSnapshot snapshot,
            RefreshReason reason,
            Instant lastAttemptAt,
            boolean inFlight) {
        if (module == TaiModule.SYSTEM
                || module == TaiModule.UI_GATEWAY
                || module == TaiModule.AVATAR) {
            return false;
        }

        if (!properties.hasEndpoint(module)) {
            return false;
        }

        if (snapshot == null) {
            return false;
        }

        if (snapshot.health() == ModuleHealth.DISABLED) {
            return false;
        }

        if (inFlight) {
            return false;
        }

        Instant now = clock.instant();

        if (lastAttemptAt != null
                && lastAttemptAt.plus(properties.getMinDelayBetweenAttempts()).isAfter(now)) {
            return false;
        }

        if (reason == RefreshReason.ERROR) {
            return lastAttemptAt == null
                    || lastAttemptAt.plus(properties.getErrorRefreshMinDelay()).isBefore(now)
                    || lastAttemptAt.plus(properties.getErrorRefreshMinDelay()).equals(now);
        }

        if (reason == RefreshReason.STARTUP) {
            return true;
        }

        return isHealthStale(snapshot, now);
    }

    public boolean isHealthStale(ModuleRuntimeSnapshot snapshot, Instant now) {
        if (snapshot == null) {
            return true;
        }

        if (snapshot.health() == ModuleHealth.DISABLED) {
            return false;
        }

        if (snapshot.lastHealthAt() == null) {
            return true;
        }

        return snapshot.lastHealthAt().plus(properties.getFreshnessThreshold()).isBefore(now);
    }
}
