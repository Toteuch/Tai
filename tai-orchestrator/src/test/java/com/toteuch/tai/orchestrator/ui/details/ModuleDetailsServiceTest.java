// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.details;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.ui.model.ModuleDetails;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeStateLabelMapper;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModuleDetailsServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant HEALTH_AT = Instant.parse("2026-05-01T10:00:05Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void get_details_should_return_current_module_runtime_snapshot() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);
        ModuleDetailsService service =
                new ModuleDetailsService(registry, new ModuleRuntimeStateLabelMapper());

        updater.ttsSpeaking("correlation-id");
        updater.updateHealth(
                TaiModule.TTS_PIPER,
                "UP",
                null,
                HEALTH_AT,
                null,
                Map.of("voiceId", "en_GB-alba-medium", "responseTimeMs", 12L));

        ModuleDetails details = service.getDetails(TaiModule.TTS_PIPER);

        assertThat(details.module()).isEqualTo(TaiModule.TTS_PIPER);
        assertThat(details.health()).isEqualTo(ModuleHealth.UP);
        assertThat(details.state()).isEqualTo("Speaking");
        assertThat(details.checkedAt()).isEqualTo(HEALTH_AT);
        assertThat(details.lastCorrelationId()).isEqualTo("correlation-id");
        assertThat(details.lastProcessTimeMs()).isEqualTo(12L);
        assertThat(details.lastError()).isNull();
        assertThat(details.details()).containsEntry("voiceId", "en_GB-alba-medium");
    }

    @Test
    void get_details_should_fallback_to_activity_timestamp_when_health_was_never_checked() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);
        ModuleDetailsService service =
                new ModuleDetailsService(registry, new ModuleRuntimeStateLabelMapper());

        updater.llmGenerating("llm-correlation-id");

        ModuleDetails details = service.getDetails(TaiModule.LLM);

        assertThat(details.module()).isEqualTo(TaiModule.LLM);
        assertThat(details.health()).isEqualTo(ModuleHealth.UP);
        assertThat(details.state()).isEqualTo("Generating");
        assertThat(details.checkedAt()).isEqualTo(NOW);
        assertThat(details.lastCorrelationId()).isEqualTo("llm-correlation-id");
    }
}
