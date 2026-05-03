// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import com.toteuch.tai.orchestrator.ui.health.ModuleHealthRefreshProperties;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ModuleOverviewStalePolicy {

    private final ModuleHealthRefreshProperties properties;

    public ModuleOverviewStalePolicy(ModuleHealthRefreshProperties properties) {
        this.properties = properties;
    }

    public boolean isStale(ModuleRuntimeSnapshot snapshot, Instant now) {
        if (snapshot == null) {
            return true;
        }

        if (snapshot.module() == TaiModule.SYSTEM
                || snapshot.module() == TaiModule.ORCHESTRATOR
                || snapshot.health() == ModuleHealth.DISABLED) {
            return false;
        }

        if (snapshot.lastHealthAt() == null) {
            return true;
        }

        return snapshot.lastHealthAt().plus(properties.getFreshnessThreshold()).isBefore(now);
    }
}
