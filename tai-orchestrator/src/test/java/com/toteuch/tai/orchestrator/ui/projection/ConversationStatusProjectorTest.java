// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.ui.model.ConversationStatus;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversationStatusProjectorTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private ModuleRuntimeRegistry registry;
    private ConversationStatusProjector projector;

    @BeforeEach
    void set_up() {
        registry = new ModuleRuntimeRegistry(clock);
        projector = new ConversationStatusProjector(registry);
    }

    @Test
    void project_should_return_idle_when_no_runtime_activity_matches() {
        assertThat(projector.project()).isEqualTo(ConversationStatus.IDLE);
    }

    @Test
    void project_should_return_error_when_core_module_is_down() {
        update(TaiModule.LLM, ModuleHealth.DOWN, ModuleActivity.IDLE);

        assertThat(projector.project()).isEqualTo(ConversationStatus.ERROR);
    }

    @Test
    void project_should_return_error_when_core_module_activity_is_error() {
        update(TaiModule.TTS_PIPER, ModuleHealth.DEGRADED, ModuleActivity.ERROR);

        assertThat(projector.project()).isEqualTo(ConversationStatus.ERROR);
    }

    @Test
    void project_should_return_error_when_orchestrator_is_down() {
        update(TaiModule.ORCHESTRATOR, ModuleHealth.DOWN, ModuleActivity.IDLE);

        assertThat(projector.project()).isEqualTo(ConversationStatus.ERROR);
    }

    @Test
    void project_should_ignore_non_core_module_down_for_error_status() {
        update(TaiModule.UI_GATEWAY, ModuleHealth.DOWN, ModuleActivity.ERROR);

        assertThat(projector.project()).isEqualTo(ConversationStatus.IDLE);
    }

    @Test
    void project_should_prioritize_error_over_speaking() {
        update(TaiModule.LLM, ModuleHealth.DEGRADED, ModuleActivity.ERROR);
        update(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.SPEAKING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.ERROR);
    }

    @Test
    void project_should_return_speaking_when_tts_is_synthesizing() {
        update(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.SYNTHESIZING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.SPEAKING);
    }

    @Test
    void project_should_return_speaking_when_tts_is_speaking() {
        update(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.SPEAKING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.SPEAKING);
    }

    @Test
    void project_should_prioritize_speaking_over_thinking_and_listening() {
        update(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.LISTENING);
        update(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.GENERATING);
        update(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.SPEAKING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.SPEAKING);
    }

    @Test
    void project_should_return_thinking_when_llm_is_generating() {
        update(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.GENERATING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.THINKING);
    }

    @Test
    void project_should_return_thinking_when_stt_listener_is_processing() {
        update(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.PROCESSING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.THINKING);
    }

    @Test
    void project_should_return_thinking_when_stt_whisper_is_processing() {
        update(TaiModule.STT_WHISPER, ModuleHealth.UP, ModuleActivity.PROCESSING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.THINKING);
    }

    @Test
    void project_should_prioritize_thinking_over_listening() {
        update(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.LISTENING);
        update(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.GENERATING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.THINKING);
    }

    @Test
    void project_should_return_listening_when_stt_listener_is_listening() {
        update(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.LISTENING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.LISTENING);
    }

    @Test
    void project_should_return_listening_when_stt_listener_is_capturing() {
        update(TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.CAPTURING);

        assertThat(projector.project()).isEqualTo(ConversationStatus.LISTENING);
    }

    private void update(TaiModule module, ModuleHealth health, ModuleActivity activity) {
        registry.update(
                new ModuleRuntimeSnapshot(
                        module, health, activity, NOW, NOW, "correlation-id", null, Map.of()));
    }
}
