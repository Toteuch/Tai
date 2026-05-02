// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.runtime;

import static org.junit.jupiter.api.Assertions.*;

import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModuleRuntimeRegistryTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void should_initialize_default_snapshots() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);

        Map<TaiModule, ModuleRuntimeSnapshot> snapshots = registry.snapshotAll();

        assertEquals(TaiModule.values().length, snapshots.size());

        ModuleRuntimeSnapshot system = registry.get(TaiModule.SYSTEM);
        assertEquals(ModuleHealth.DEGRADED, system.health());
        assertNull(system.lastActivity());
        assertNull(system.lastActivityAt());
        assertEquals(NOW, system.lastHealthAt());
        assertTrue(system.details().isEmpty());

        ModuleRuntimeSnapshot orchestrator = registry.get(TaiModule.ORCHESTRATOR);
        assertEquals(ModuleHealth.UP, orchestrator.health());
        assertEquals(ModuleActivity.PROCESSING, orchestrator.lastActivity());
        assertEquals(NOW, orchestrator.lastActivityAt());
        assertNull(orchestrator.lastHealthAt());

        ModuleRuntimeSnapshot uiGateway = registry.get(TaiModule.UI_GATEWAY);
        assertEquals(ModuleHealth.DISABLED, uiGateway.health());
        assertEquals(ModuleActivity.DISABLED, uiGateway.lastActivity());

        ModuleRuntimeSnapshot avatar = registry.get(TaiModule.AVATAR);
        assertEquals(ModuleHealth.DISABLED, avatar.health());
        assertEquals(ModuleActivity.DISABLED, avatar.lastActivity());
    }

    @Test
    void snapshot_all_should_return_unmodifiable_map() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);

        Map<TaiModule, ModuleRuntimeSnapshot> snapshots = registry.snapshotAll();

        assertThrows(UnsupportedOperationException.class, () -> snapshots.clear());
    }

    @Test
    void should_recompute_system_health_when_module_is_updated() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);

        registry.update(
                snapshot(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.LISTENING));
        registry.update(snapshot(TaiModule.STT_WHISPER, ModuleHealth.UP, ModuleActivity.IDLE));
        registry.update(snapshot(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.IDLE));
        registry.update(snapshot(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.IDLE));

        assertEquals(ModuleHealth.UP, registry.get(TaiModule.SYSTEM).health());

        registry.update(snapshot(TaiModule.LLM, ModuleHealth.DEGRADED, ModuleActivity.ERROR));

        assertEquals(ModuleHealth.DEGRADED, registry.get(TaiModule.SYSTEM).health());

        registry.update(snapshot(TaiModule.TTS_PIPER, ModuleHealth.DOWN, ModuleActivity.ERROR));

        assertEquals(ModuleHealth.DOWN, registry.get(TaiModule.SYSTEM).health());
    }

    @Test
    void system_health_should_ignore_disabled_modules() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);

        registry.update(
                snapshot(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.LISTENING));
        registry.update(snapshot(TaiModule.STT_WHISPER, ModuleHealth.UP, ModuleActivity.IDLE));
        registry.update(snapshot(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.IDLE));
        registry.update(snapshot(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.IDLE));

        assertEquals(ModuleHealth.DISABLED, registry.get(TaiModule.UI_GATEWAY).health());
        assertEquals(ModuleHealth.DISABLED, registry.get(TaiModule.AVATAR).health());
        assertEquals(ModuleHealth.UP, registry.get(TaiModule.SYSTEM).health());
    }

    @Test
    void update_should_not_trigger_recursive_system_update() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);

        ModuleRuntimeSnapshot manualSystemSnapshot =
                new ModuleRuntimeSnapshot(
                        TaiModule.SYSTEM,
                        ModuleHealth.DOWN,
                        null,
                        null,
                        NOW,
                        null,
                        "manual-error",
                        Map.of("source", "test"));

        registry.update(manualSystemSnapshot);

        ModuleRuntimeSnapshot system = registry.get(TaiModule.SYSTEM);

        assertEquals(ModuleHealth.DOWN, system.health());
        assertEquals("manual-error", system.lastError());
        assertEquals("test", system.details().get("source"));
    }

    private ModuleRuntimeSnapshot snapshot(
            TaiModule module, ModuleHealth health, ModuleActivity activity) {
        return new ModuleRuntimeSnapshot(
                module, health, activity, NOW, NOW, "correlation-id", null, Map.of());
    }
}
