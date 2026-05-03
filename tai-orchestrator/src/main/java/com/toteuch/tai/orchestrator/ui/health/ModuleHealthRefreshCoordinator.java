// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "tai.ui.health-refresh",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ModuleHealthRefreshCoordinator {

    private final ModuleRuntimeRegistry registry;
    private final ModuleRuntimeUpdater updater;
    private final ModuleHealthClient healthClient;
    private final ModuleHealthRefreshPolicy policy;
    private final ModuleHealthRefreshListener listener;
    private final Executor executor;
    private final Clock clock;

    private final Object monitor = new Object();
    private final EnumSet<TaiModule> inFlight = EnumSet.noneOf(TaiModule.class);
    private final EnumMap<TaiModule, Instant> lastAttemptAt = new EnumMap<>(TaiModule.class);

    public ModuleHealthRefreshCoordinator(
            ModuleRuntimeRegistry registry,
            ModuleRuntimeUpdater updater,
            ModuleHealthClient healthClient,
            ModuleHealthRefreshPolicy policy,
            ModuleHealthRefreshListener listener,
            @Qualifier("moduleHealthRefreshExecutor") Executor executor,
            Clock clock) {
        this.registry = registry;
        this.updater = updater;
        this.healthClient = healthClient;
        this.policy = policy;
        this.listener = listener;
        this.executor = executor;
        this.clock = clock;
    }

    public void refreshStaleModulesAsync() {
        for (Map.Entry<TaiModule, ModuleRuntimeSnapshot> entry :
                registry.snapshotAll().entrySet()) {
            requestRefreshIfAllowed(entry.getKey(), RefreshReason.SCHEDULED);
        }
    }

    public void refreshAllAsync(RefreshReason reason) {
        for (TaiModule module : registry.snapshotAll().keySet()) {
            requestRefreshIfAllowed(module, reason);
        }
    }

    @EventListener
    public void onModuleRuntimeError(ModuleRuntimeErrorEvent event) {
        requestImmediateRefresh(event.module(), RefreshReason.ERROR);
    }

    public void requestImmediateRefresh(TaiModule module, RefreshReason reason) {
        requestRefreshIfAllowed(module, reason);
    }

    private void requestRefreshIfAllowed(TaiModule module, RefreshReason reason) {
        boolean accepted;

        synchronized (monitor) {
            ModuleRuntimeSnapshot snapshot = registry.get(module);
            Instant lastAttempt = lastAttemptAt.get(module);
            boolean moduleInFlight = inFlight.contains(module);

            accepted = policy.shouldRefresh(module, snapshot, reason, lastAttempt, moduleInFlight);

            if (accepted) {
                inFlight.add(module);
                lastAttemptAt.put(module, clock.instant());
            }
        }

        if (!accepted) {
            return;
        }

        executor.execute(() -> refresh(module, reason));
    }

    private void refresh(TaiModule module, RefreshReason reason) {
        try {
            ModuleHealthResult result = healthClient.check(module);

            updater.updateHealth(
                    result.module(),
                    result.status(),
                    activityFromHealth(result, result.module()),
                    result.respondedAt(),
                    result.error(),
                    result.details());

            listener.onModuleHealthRefreshed(module, reason);
        } finally {
            synchronized (monitor) {
                inFlight.remove(module);
            }
        }
    }

    private ModuleActivity activityFromHealth(ModuleHealthResult result, TaiModule module) {
        if ("DOWN".equalsIgnoreCase(result.status())) {
            return ModuleActivity.ERROR;
        }
        if (module == TaiModule.STT_LISTENER) {
            if ("DEGRADED".equalsIgnoreCase(result.status())) {
                return ModuleActivity.IDLE;
            }
        }

        return result.activity();
    }
}
