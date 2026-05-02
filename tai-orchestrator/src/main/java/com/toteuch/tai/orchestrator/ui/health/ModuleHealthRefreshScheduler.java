// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "tai.ui.health-refresh",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ModuleHealthRefreshScheduler {

    private final ModuleHealthRefreshCoordinator coordinator;

    public ModuleHealthRefreshScheduler(ModuleHealthRefreshCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Scheduled(fixedDelayString = "${tai.ui.health-refresh.interval:5s}")
    public void refreshStaleModules() {
        coordinator.refreshStaleModulesAsync();
    }
}
