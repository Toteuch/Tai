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

class ModuleRuntimeUpdaterTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant RESPONDED_AT = Instant.parse("2026-05-01T10:00:05Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void tts_speaking_should_mark_tts_piper_up_speaking_and_set_correlation_id() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.ttsSpeaking("tts-correlation-id");

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.TTS_PIPER);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertEquals(ModuleActivity.SPEAKING, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertNull(snapshot.lastHealthAt());
        assertEquals("tts-correlation-id", snapshot.lastActiveCorrelationId());
        assertNull(snapshot.lastError());
        assertTrue(snapshot.details().isEmpty());
    }

    @Test
    void tts_idle_should_keep_previous_last_active_correlation_id() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.ttsSpeaking("tts-correlation-id");
        updater.ttsIdle();

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.TTS_PIPER);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertEquals(ModuleActivity.IDLE, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals("tts-correlation-id", snapshot.lastActiveCorrelationId());
    }

    @Test
    void llm_generating_should_mark_llm_up_generating_and_set_correlation_id() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.llmGenerating("llm-correlation-id");

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.LLM);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertEquals(ModuleActivity.GENERATING, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals("llm-correlation-id", snapshot.lastActiveCorrelationId());
    }

    @Test
    void stt_listener_capturing_should_mark_stt_listener_up_capturing_and_set_correlation_id() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.sttListenerCapturing("stt-correlation-id");

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.STT_LISTENER);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertEquals(ModuleActivity.CAPTURING, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals("stt-correlation-id", snapshot.lastActiveCorrelationId());
    }

    @Test
    void stt_whisper_transcribing_should_mark_stt_whisper_up_processing_and_set_correlation_id() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.sttWhisperTranscribing("stt-correlation-id");

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.STT_WHISPER);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertEquals(ModuleActivity.PROCESSING, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals("stt-correlation-id", snapshot.lastActiveCorrelationId());
    }

    @Test
    void error_methods_should_mark_modules_degraded_with_error_activity() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.ttsError();
        updater.llmError();
        updater.sttWhisperError();

        ModuleRuntimeSnapshot tts = registry.get(TaiModule.TTS_PIPER);
        assertEquals(ModuleHealth.DEGRADED, tts.health());
        assertEquals(ModuleActivity.ERROR, tts.lastActivity());

        ModuleRuntimeSnapshot llm = registry.get(TaiModule.LLM);
        assertEquals(ModuleHealth.DEGRADED, llm.health());
        assertEquals(ModuleActivity.ERROR, llm.lastActivity());

        ModuleRuntimeSnapshot whisper = registry.get(TaiModule.STT_WHISPER);
        assertEquals(ModuleHealth.DEGRADED, whisper.health());
        assertEquals(ModuleActivity.ERROR, whisper.lastActivity());
    }

    @Test
    void
            update_health_should_update_health_timestamp_details_and_preserve_activity_when_activity_is_null() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.llmGenerating("llm-correlation-id");

        updater.updateHealth(
                TaiModule.LLM,
                "DOWN",
                null,
                RESPONDED_AT,
                "ollama unavailable",
                Map.of("model", "tai-llama"));

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.LLM);

        assertEquals(ModuleHealth.DOWN, snapshot.health());
        assertEquals(ModuleActivity.GENERATING, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals(RESPONDED_AT, snapshot.lastHealthAt());
        assertEquals("llm-correlation-id", snapshot.lastActiveCorrelationId());
        assertEquals("ollama unavailable", snapshot.lastError());
        assertEquals("tai-llama", snapshot.details().get("model"));
    }

    @Test
    void update_health_should_set_activity_and_activity_timestamp_when_activity_is_provided() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.updateHealth(
                TaiModule.STT_LISTENER,
                "UP",
                ModuleActivity.LISTENING,
                RESPONDED_AT,
                null,
                Map.of("running", true));

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.STT_LISTENER);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertEquals(ModuleActivity.LISTENING, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals(RESPONDED_AT, snapshot.lastHealthAt());
        assertNull(snapshot.lastActiveCorrelationId());
        assertNull(snapshot.lastError());
        assertEquals(true, snapshot.details().get("running"));
    }

    @Test
    void update_health_should_mark_modules_degraded_with_timeout_activity() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.updateHealth(
                TaiModule.TTS_PIPER, "TIMEOUT", null, RESPONDED_AT, "request timed out", Map.of());

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.TTS_PIPER);

        assertEquals(ModuleHealth.DEGRADED, snapshot.health());
        assertEquals(ModuleActivity.UNKNOWN, snapshot.lastActivity());
        assertEquals(NOW, snapshot.lastActivityAt());
        assertEquals(RESPONDED_AT, snapshot.lastHealthAt());
        assertEquals("request timed out", snapshot.lastError());
        assertTrue(snapshot.details().isEmpty());
    }

    @Test
    void update_health_should_mark_modules_up_with_no_activity() {
        ModuleRuntimeRegistry registry = new ModuleRuntimeRegistry(clock);
        ModuleRuntimeUpdater updater = new ModuleRuntimeUpdater(registry, clock);

        updater.updateHealth(TaiModule.TTS_PIPER, "UP", null, RESPONDED_AT, null, null);

        ModuleRuntimeSnapshot snapshot = registry.get(TaiModule.TTS_PIPER);

        assertEquals(ModuleHealth.UP, snapshot.health());
        assertTrue(snapshot.details().isEmpty());
    }
}
