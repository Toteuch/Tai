// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "tai.ui.health-refresh",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ModuleHealthRefreshStartup {

    private final ModuleHealthRefreshCoordinator coordinator;

    public ModuleHealthRefreshStartup(ModuleHealthRefreshCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartup() {
        coordinator.refreshAllAsync(RefreshReason.STARTUP);
    }
}
