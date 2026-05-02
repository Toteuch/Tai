// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.runtime;

import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.time.Clock;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ModuleRuntimeRegistry {
    private final EnumMap<TaiModule, AtomicReference<ModuleRuntimeSnapshot>> snapshots;
    private final Clock clock;

    public ModuleRuntimeRegistry(Clock clock) {
        this.snapshots = new EnumMap<>(TaiModule.class);

        ModuleRuntimeDefaults.initialSnapshots(clock)
                .forEach(
                        (module, snapshot) ->
                                this.snapshots.put(module, new AtomicReference<>(snapshot)));
        this.clock = clock;
    }

    public ModuleRuntimeSnapshot get(TaiModule module) {
        AtomicReference<ModuleRuntimeSnapshot> ref = snapshots.get(module);
        return ref == null ? null : ref.get();
    }

    public Map<TaiModule, ModuleRuntimeSnapshot> snapshotAll() {
        return snapshots.entrySet().stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    public void update(ModuleRuntimeSnapshot snapshot) {
        snapshots
                .computeIfAbsent(snapshot.module(), ignored -> new AtomicReference<>())
                .set(snapshot);
        if (snapshot.module() != TaiModule.SYSTEM) {
            updateSystem();
        }
    }

    private void updateSystem() {
        ModuleHealth health =
                snapshots.entrySet().stream()
                        .filter(entry -> entry.getKey() != TaiModule.SYSTEM)
                        .map(entry -> entry.getValue().get().health())
                        .filter(moduleHealth -> moduleHealth != ModuleHealth.DISABLED)
                        .reduce(ModuleHealth.UP, this::worstHealth);

        update(
                new ModuleRuntimeSnapshot(
                        TaiModule.SYSTEM,
                        health,
                        null,
                        null,
                        clock.instant(),
                        null,
                        null,
                        Map.of()));
    }

    private ModuleHealth worstHealth(ModuleHealth left, ModuleHealth right) {
        if (left == ModuleHealth.DOWN || right == ModuleHealth.DOWN) {
            return ModuleHealth.DOWN;
        }
        if (left == ModuleHealth.DEGRADED || right == ModuleHealth.DEGRADED) {
            return ModuleHealth.DEGRADED;
        }
        return ModuleHealth.UP;
    }

    private static class ModuleRuntimeDefaults {
        public static EnumMap<TaiModule, ModuleRuntimeSnapshot> initialSnapshots(Clock clock) {
            EnumMap<TaiModule, ModuleRuntimeSnapshot> defaults = new EnumMap<>(TaiModule.class);
            defaults.put(
                    TaiModule.SYSTEM,
                    new ModuleRuntimeSnapshot(
                            TaiModule.SYSTEM,
                            ModuleHealth.DEGRADED,
                            null,
                            null,
                            clock.instant(),
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.ORCHESTRATOR,
                    new ModuleRuntimeSnapshot(
                            TaiModule.ORCHESTRATOR,
                            ModuleHealth.UP,
                            ModuleActivity.PROCESSING,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.STT_LISTENER,
                    new ModuleRuntimeSnapshot(
                            TaiModule.STT_LISTENER,
                            ModuleHealth.DEGRADED,
                            ModuleActivity.UNKNOWN,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.STT_WHISPER,
                    new ModuleRuntimeSnapshot(
                            TaiModule.STT_WHISPER,
                            ModuleHealth.DEGRADED,
                            ModuleActivity.UNKNOWN,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.LLM,
                    new ModuleRuntimeSnapshot(
                            TaiModule.LLM,
                            ModuleHealth.DEGRADED,
                            ModuleActivity.UNKNOWN,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.TTS_PIPER,
                    new ModuleRuntimeSnapshot(
                            TaiModule.TTS_PIPER,
                            ModuleHealth.DEGRADED,
                            ModuleActivity.UNKNOWN,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.UI_GATEWAY,
                    new ModuleRuntimeSnapshot(
                            TaiModule.UI_GATEWAY,
                            ModuleHealth.DISABLED,
                            ModuleActivity.DISABLED,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            defaults.put(
                    TaiModule.AVATAR,
                    new ModuleRuntimeSnapshot(
                            TaiModule.AVATAR,
                            ModuleHealth.DISABLED,
                            ModuleActivity.DISABLED,
                            clock.instant(),
                            null,
                            null,
                            null,
                            Map.of()));
            return defaults;
        }
    }
}
