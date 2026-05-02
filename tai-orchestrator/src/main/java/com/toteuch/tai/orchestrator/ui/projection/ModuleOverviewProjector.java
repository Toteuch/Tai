// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import com.toteuch.tai.orchestrator.ui.model.ModuleOverview;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeStateLabelMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ModuleOverviewProjector {

    private final ModuleRuntimeRegistry registry;
    private final ModuleRuntimeStateLabelMapper stateLabelMapper;
    private final ModuleOverviewStalePolicy stalePolicy;
    private final Clock clock;

    public ModuleOverviewProjector(
            ModuleRuntimeRegistry registry,
            ModuleRuntimeStateLabelMapper stateLabelMapper,
            ModuleOverviewStalePolicy stalePolicy,
            Clock clock) {
        this.registry = registry;
        this.stateLabelMapper = stateLabelMapper;
        this.stalePolicy = stalePolicy;
        this.clock = clock;
    }

    public Map<TaiModule, ModuleOverview> project() {
        Instant now = clock.instant();
        EnumMap<TaiModule, ModuleOverview> result = new EnumMap<>(TaiModule.class);

        registry.snapshotAll()
                .forEach((module, snapshot) -> result.put(module, toOverview(snapshot, now)));

        return Map.copyOf(result);
    }

    private ModuleOverview toOverview(ModuleRuntimeSnapshot snapshot, Instant now) {
        return new ModuleOverview(
                snapshot.health(),
                stateLabelMapper.toStateLabel(snapshot),
                lastUpdateAt(snapshot),
                stalePolicy.isStale(snapshot, now));
    }

    private Instant lastUpdateAt(ModuleRuntimeSnapshot snapshot) {
        if (snapshot.lastActivityAt() == null) {
            return snapshot.lastHealthAt();
        }

        if (snapshot.lastHealthAt() == null) {
            return snapshot.lastActivityAt();
        }

        return snapshot.lastActivityAt().isAfter(snapshot.lastHealthAt())
                ? snapshot.lastActivityAt()
                : snapshot.lastHealthAt();
    }
}
