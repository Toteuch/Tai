// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.ui.health.ModuleHealthRefreshProperties;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.ModuleOverview;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeStateLabelMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleOverviewProjectorTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant FRESH_HEALTH_AT = NOW.minusSeconds(5);
    private static final Instant STALE_HEALTH_AT = NOW.minusSeconds(20);
    private static final Instant ACTIVITY_AT = NOW.minusSeconds(1);

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private ModuleRuntimeRegistry registry;
    private ModuleOverviewProjector projector;

    @BeforeEach
    void set_up() {
        registry = new ModuleRuntimeRegistry(clock);

        ModuleHealthRefreshProperties properties = new ModuleHealthRefreshProperties();
        properties.setFreshnessThreshold(Duration.ofSeconds(15));

        projector =
                new ModuleOverviewProjector(
                        registry,
                        new ModuleRuntimeStateLabelMapper(),
                        new ModuleOverviewStalePolicy(properties),
                        clock);
    }

    @Test
    void project_should_return_one_overview_per_runtime_module() {
        Map<TaiModule, ModuleOverview> modules = projector.project();

        assertThat(modules).containsOnlyKeys(TaiModule.values());
    }

    @Test
    void project_should_project_health_state_last_update_and_freshness_for_fresh_module() {
        update(
                TaiModule.LLM,
                ModuleHealth.UP,
                ModuleActivity.GENERATING,
                ACTIVITY_AT,
                FRESH_HEALTH_AT,
                "correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.LLM);

        assertThat(overview.health()).isEqualTo(ModuleHealth.UP);
        assertThat(overview.state()).isEqualTo("Generating");
        assertThat(overview.lastUpdateAt()).isEqualTo(ACTIVITY_AT);
        assertThat(overview.stale()).isFalse();
    }

    @Test
    void project_should_mark_module_stale_when_last_health_is_older_than_threshold() {
        update(
                TaiModule.LLM,
                ModuleHealth.UP,
                ModuleActivity.GENERATING,
                ACTIVITY_AT,
                STALE_HEALTH_AT,
                "correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.LLM);

        assertThat(overview.health()).isEqualTo(ModuleHealth.UP);
        assertThat(overview.state()).isEqualTo("Generating");
        assertThat(overview.lastUpdateAt()).isEqualTo(ACTIVITY_AT);
        assertThat(overview.stale()).isTrue();
    }

    @Test
    void project_should_mark_module_stale_when_health_was_never_checked() {
        update(
                TaiModule.TTS_PIPER,
                ModuleHealth.UP,
                ModuleActivity.SPEAKING,
                ACTIVITY_AT,
                null,
                "correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.TTS_PIPER);

        assertThat(overview.health()).isEqualTo(ModuleHealth.UP);
        assertThat(overview.state()).isEqualTo("Speaking");
        assertThat(overview.lastUpdateAt()).isEqualTo(ACTIVITY_AT);
        assertThat(overview.stale()).isTrue();
    }

    @Test
    void project_should_use_health_timestamp_as_last_update_when_activity_timestamp_is_null() {
        update(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.UNKNOWN, null, FRESH_HEALTH_AT, null);

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.LLM);

        assertThat(overview.lastUpdateAt()).isEqualTo(FRESH_HEALTH_AT);
        assertThat(overview.stale()).isFalse();
    }

    @Test
    void project_should_use_activity_timestamp_as_last_update_when_health_timestamp_is_null() {
        update(
                TaiModule.LLM,
                ModuleHealth.UP,
                ModuleActivity.GENERATING,
                ACTIVITY_AT,
                null,
                "correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.LLM);

        assertThat(overview.lastUpdateAt()).isEqualTo(ACTIVITY_AT);
        assertThat(overview.stale()).isTrue();
    }

    @Test
    void project_should_use_latest_timestamp_as_last_update_when_activity_is_after_health() {
        update(
                TaiModule.TTS_PIPER,
                ModuleHealth.UP,
                ModuleActivity.SPEAKING,
                ACTIVITY_AT,
                FRESH_HEALTH_AT,
                "correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.TTS_PIPER);

        assertThat(overview.lastUpdateAt()).isEqualTo(ACTIVITY_AT);
    }

    @Test
    void project_should_use_latest_timestamp_as_last_update_when_health_is_after_activity() {
        Instant activityAt = NOW.minusSeconds(10);
        Instant healthAt = NOW.minusSeconds(2);

        update(
                TaiModule.TTS_PIPER,
                ModuleHealth.UP,
                ModuleActivity.SPEAKING,
                activityAt,
                healthAt,
                "correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.TTS_PIPER);

        assertThat(overview.lastUpdateAt()).isEqualTo(healthAt);
    }

    @Test
    void project_should_not_mark_disabled_module_as_stale() {
        update(
                TaiModule.AVATAR,
                ModuleHealth.DISABLED,
                ModuleActivity.DISABLED,
                STALE_HEALTH_AT,
                STALE_HEALTH_AT,
                null);

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.AVATAR);

        assertThat(overview.health()).isEqualTo(ModuleHealth.DISABLED);
        assertThat(overview.state()).isEqualTo("Disabled");
        assertThat(overview.stale()).isFalse();
    }

    @Test
    void project_should_not_mark_system_as_stale() {
        update(TaiModule.SYSTEM, ModuleHealth.DOWN, null, null, STALE_HEALTH_AT, null);

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.SYSTEM);

        assertThat(overview.health()).isEqualTo(ModuleHealth.DOWN);
        assertThat(overview.state()).isEqualTo("Down");
        assertThat(overview.stale()).isFalse();
    }

    @Test
    void project_should_not_mark_orchestrator_as_stale() {
        update(
                TaiModule.ORCHESTRATOR,
                ModuleHealth.UP,
                ModuleActivity.IDLE,
                STALE_HEALTH_AT,
                STALE_HEALTH_AT,
                null);

        Map<TaiModule, ModuleOverview> modules = projector.project();

        ModuleOverview overview = modules.get(TaiModule.ORCHESTRATOR);

        assertThat(overview.health()).isEqualTo(ModuleHealth.UP);
        assertThat(overview.state()).isEqualTo("Active");
        assertThat(overview.stale()).isFalse();
    }

    @Test
    void project_should_map_runtime_activity_to_module_state_labels() {
        update(
                TaiModule.STT_LISTENER,
                ModuleHealth.UP,
                ModuleActivity.CAPTURING,
                ACTIVITY_AT,
                FRESH_HEALTH_AT,
                "stt-correlation-id");

        update(
                TaiModule.STT_WHISPER,
                ModuleHealth.UP,
                ModuleActivity.PROCESSING,
                ACTIVITY_AT,
                FRESH_HEALTH_AT,
                "whisper-correlation-id");

        update(
                TaiModule.TTS_PIPER,
                ModuleHealth.UP,
                ModuleActivity.IDLE,
                ACTIVITY_AT,
                FRESH_HEALTH_AT,
                "tts-correlation-id");

        Map<TaiModule, ModuleOverview> modules = projector.project();

        assertThat(modules.get(TaiModule.STT_LISTENER).state()).isEqualTo("Recording");
        assertThat(modules.get(TaiModule.STT_WHISPER).state()).isEqualTo("Transcribing");
        assertThat(modules.get(TaiModule.TTS_PIPER).state()).isEqualTo("Silent");
    }

    private void update(
            TaiModule module,
            ModuleHealth health,
            ModuleActivity activity,
            Instant lastActivityAt,
            Instant lastHealthAt,
            String correlationId) {
        registry.update(
                new ModuleRuntimeSnapshot(
                        module,
                        health,
                        activity,
                        lastActivityAt,
                        lastHealthAt,
                        correlationId,
                        null,
                        Map.of()));
    }
}
