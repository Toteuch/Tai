// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.ui.health.ModuleHealthRefreshProperties;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleOverviewStalePolicyTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");

    private ModuleOverviewStalePolicy policy;

    @BeforeEach
    void set_up() {
        ModuleHealthRefreshProperties properties = new ModuleHealthRefreshProperties();
        properties.setFreshnessThreshold(Duration.ofSeconds(15));

        policy = new ModuleOverviewStalePolicy(properties);
    }

    @Test
    void is_stale_should_return_true_when_snapshot_is_null() {
        assertThat(policy.isStale(null, NOW)).isTrue();
    }

    @Test
    void is_stale_should_return_false_for_disabled_module() {
        ModuleRuntimeSnapshot snapshot =
                snapshot(
                        TaiModule.AVATAR,
                        ModuleHealth.DISABLED,
                        ModuleActivity.DISABLED,
                        NOW.minusSeconds(60));

        assertThat(policy.isStale(snapshot, NOW)).isFalse();
    }

    @Test
    void is_stale_should_return_false_for_system_even_when_health_is_old() {
        ModuleRuntimeSnapshot snapshot =
                snapshot(TaiModule.SYSTEM, ModuleHealth.DEGRADED, null, NOW.minusSeconds(60));

        assertThat(policy.isStale(snapshot, NOW)).isFalse();
    }

    @Test
    void is_stale_should_return_false_for_orchestrator_even_when_health_is_old() {
        ModuleRuntimeSnapshot snapshot =
                snapshot(
                        TaiModule.ORCHESTRATOR,
                        ModuleHealth.UP,
                        ModuleActivity.IDLE,
                        NOW.minusSeconds(60));

        assertThat(policy.isStale(snapshot, NOW)).isFalse();
    }

    @Test
    void is_stale_should_return_true_when_health_was_never_checked() {
        ModuleRuntimeSnapshot snapshot =
                new ModuleRuntimeSnapshot(
                        TaiModule.LLM,
                        ModuleHealth.UP,
                        ModuleActivity.GENERATING,
                        NOW,
                        null,
                        "correlation-id",
                        null,
                        Map.of());

        assertThat(policy.isStale(snapshot, NOW)).isTrue();
    }

    @Test
    void is_stale_should_return_false_when_last_health_is_more_recent_than_threshold() {
        ModuleRuntimeSnapshot snapshot =
                snapshot(
                        TaiModule.LLM,
                        ModuleHealth.UP,
                        ModuleActivity.GENERATING,
                        NOW.minusSeconds(10));

        assertThat(policy.isStale(snapshot, NOW)).isFalse();
    }

    @Test
    void is_stale_should_return_false_when_last_health_is_exactly_at_threshold() {
        ModuleRuntimeSnapshot snapshot =
                snapshot(
                        TaiModule.LLM,
                        ModuleHealth.UP,
                        ModuleActivity.GENERATING,
                        NOW.minusSeconds(15));

        assertThat(policy.isStale(snapshot, NOW)).isFalse();
    }

    @Test
    void is_stale_should_return_true_when_last_health_is_older_than_threshold() {
        ModuleRuntimeSnapshot snapshot =
                snapshot(
                        TaiModule.LLM,
                        ModuleHealth.UP,
                        ModuleActivity.GENERATING,
                        NOW.minusSeconds(16));

        assertThat(policy.isStale(snapshot, NOW)).isTrue();
    }

    @Test
    void is_stale_should_ignore_recent_activity_when_health_is_old() {
        ModuleRuntimeSnapshot snapshot =
                new ModuleRuntimeSnapshot(
                        TaiModule.TTS_PIPER,
                        ModuleHealth.UP,
                        ModuleActivity.SPEAKING,
                        NOW.minusSeconds(1),
                        NOW.minusSeconds(60),
                        "correlation-id",
                        null,
                        Map.of());

        assertThat(policy.isStale(snapshot, NOW)).isTrue();
    }

    @Test
    void is_stale_should_depend_on_health_timestamp_not_activity_timestamp() {
        ModuleRuntimeSnapshot snapshot =
                new ModuleRuntimeSnapshot(
                        TaiModule.STT_LISTENER,
                        ModuleHealth.UP,
                        ModuleActivity.CAPTURING,
                        NOW.minusSeconds(60),
                        NOW.minusSeconds(5),
                        "correlation-id",
                        null,
                        Map.of());

        assertThat(policy.isStale(snapshot, NOW)).isFalse();
    }

    private ModuleRuntimeSnapshot snapshot(
            TaiModule module, ModuleHealth health, ModuleActivity activity, Instant lastHealthAt) {
        return new ModuleRuntimeSnapshot(
                module, health, activity, NOW, lastHealthAt, "correlation-id", null, Map.of());
    }
}
