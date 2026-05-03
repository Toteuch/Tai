// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.runtime;

import com.toteuch.tai.orchestrator.ui.health.ModuleRuntimeErrorEvent;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ModuleRuntimeUpdater {
    private final ModuleRuntimeRegistry registry;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ModuleRuntimeUpdater(ModuleRuntimeRegistry registry, Clock clock) {
        this(registry, clock, null);
    }

    @Autowired
    public ModuleRuntimeUpdater(
            ModuleRuntimeRegistry registry,
            Clock clock,
            ApplicationEventPublisher applicationEventPublisher) {
        this.registry = registry;
        this.clock = clock;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void ttsSpeaking(String correlationId) {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.TTS_PIPER,
                        ModuleHealth.UP,
                        ModuleActivity.SPEAKING,
                        correlationId);
        registry.update(snapshot);
    }

    public void ttsSynthesizing(String correlationId) {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.TTS_PIPER,
                        ModuleHealth.UP,
                        ModuleActivity.SYNTHESIZING,
                        correlationId);
        registry.update(snapshot);
    }

    public void ttsIdle() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(TaiModule.TTS_PIPER, ModuleHealth.UP, ModuleActivity.IDLE, null);
        registry.update(snapshot);
    }

    public void ttsError() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.TTS_PIPER, ModuleHealth.DEGRADED, ModuleActivity.ERROR, null);
        registry.update(snapshot);
        publishRuntimeErrorEvent(TaiModule.TTS_PIPER, snapshot);
    }

    public void llmIdle() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(TaiModule.LLM, ModuleHealth.UP, ModuleActivity.IDLE, null);
        registry.update(snapshot);
    }

    public void llmError() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(TaiModule.LLM, ModuleHealth.DEGRADED, ModuleActivity.ERROR, null);
        registry.update(snapshot);
        publishRuntimeErrorEvent(TaiModule.LLM, snapshot);
    }

    public void llmGenerating(String correlationId) {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.LLM, ModuleHealth.UP, ModuleActivity.GENERATING, correlationId);
        registry.update(snapshot);
    }

    public void sttListenerListening() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.STT_LISTENER, ModuleHealth.UP, ModuleActivity.LISTENING, null);
        registry.update(snapshot);
    }

    public void sttListenerCapturing(String correlationId) {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.STT_LISTENER,
                        ModuleHealth.UP,
                        ModuleActivity.CAPTURING,
                        correlationId);
        registry.update(snapshot);
    }

    public void sttListenerProcessing(String correlationId) {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.STT_LISTENER,
                        ModuleHealth.UP,
                        ModuleActivity.PROCESSING,
                        correlationId);
        registry.update(snapshot);
    }

    public void sttWhisperTranscribing(String correlationId) {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.STT_WHISPER,
                        ModuleHealth.UP,
                        ModuleActivity.PROCESSING,
                        correlationId);
        registry.update(snapshot);
    }

    public void sttWhisperError() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(
                        TaiModule.STT_WHISPER, ModuleHealth.DEGRADED, ModuleActivity.ERROR, null);
        registry.update(snapshot);
        publishRuntimeErrorEvent(TaiModule.STT_WHISPER, snapshot);
    }

    public void sttWhisperIdle() {
        ModuleRuntimeSnapshot snapshot =
                buildActivity(TaiModule.STT_WHISPER, ModuleHealth.UP, ModuleActivity.IDLE, null);
        registry.update(snapshot);
    }

    private ModuleHealth parseStatus(String status) {
        return switch (status) {
            case "UP" -> ModuleHealth.UP;
            case "DOWN" -> ModuleHealth.DOWN;
            default -> ModuleHealth.DEGRADED;
        };
    }

    public void updateHealth(
            TaiModule module,
            String status,
            ModuleActivity activity,
            Instant respondedAt,
            String error,
            Map<String, Object> details) {
        ModuleRuntimeSnapshot snapshot = registry.get(module);
        String lastActiveCorrelationId = snapshot.lastActiveCorrelationId();
        String lastError = snapshot.lastError();
        ModuleActivity lastActivity = snapshot.lastActivity();
        Instant lastActivityAt = snapshot.lastActivityAt();
        registry.update(
                new ModuleRuntimeSnapshot(
                        module,
                        parseStatus(status),
                        activity != null ? activity : lastActivity,
                        activity != null ? clock.instant() : lastActivityAt,
                        respondedAt,
                        lastActiveCorrelationId,
                        error != null ? error : lastError,
                        details != null ? details : Map.of()));
    }

    private ModuleRuntimeSnapshot buildActivity(
            TaiModule module, ModuleHealth health, ModuleActivity activity, String correlationId) {
        ModuleRuntimeSnapshot snapshot = registry.get(module);
        Map<String, Object> details = snapshot.details();
        String lastError = snapshot.lastError();
        Instant lastHealthAt = snapshot.lastHealthAt();
        String lastActiveCorrelationId = snapshot.lastActiveCorrelationId();
        return new ModuleRuntimeSnapshot(
                module,
                health,
                activity,
                clock.instant(),
                lastHealthAt,
                correlationId != null ? correlationId : lastActiveCorrelationId,
                lastError,
                details);
    }

    private void publishRuntimeErrorEvent(TaiModule module, ModuleRuntimeSnapshot snapshot) {
        if (applicationEventPublisher == null) {
            return;
        }

        applicationEventPublisher.publishEvent(
                new ModuleRuntimeErrorEvent(
                        module, snapshot.lastActiveCorrelationId(), clock.instant()));
    }
}
