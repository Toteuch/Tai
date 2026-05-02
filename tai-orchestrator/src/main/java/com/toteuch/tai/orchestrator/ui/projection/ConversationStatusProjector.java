// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import com.toteuch.tai.orchestrator.ui.model.ConversationStatus;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ConversationStatusProjector {

    private static final Set<TaiModule> CORE_MODULES =
            Set.of(
                    TaiModule.ORCHESTRATOR,
                    TaiModule.STT_LISTENER,
                    TaiModule.STT_WHISPER,
                    TaiModule.LLM,
                    TaiModule.TTS_PIPER);

    private final ModuleRuntimeRegistry registry;

    public ConversationStatusProjector(ModuleRuntimeRegistry registry) {
        this.registry = registry;
    }

    public ConversationStatus project() {
        if (hasBlockingError()) {
            return ConversationStatus.ERROR;
        }

        if (isSpeaking()) {
            return ConversationStatus.SPEAKING;
        }

        if (isThinking()) {
            return ConversationStatus.THINKING;
        }

        if (isListening()) {
            return ConversationStatus.LISTENING;
        }

        return ConversationStatus.IDLE;
    }

    private boolean hasBlockingError() {
        return CORE_MODULES.stream()
                .map(registry::get)
                .anyMatch(
                        snapshot ->
                                snapshot != null
                                        && (snapshot.health() == ModuleHealth.DOWN
                                                || snapshot.lastActivity()
                                                        == ModuleActivity.ERROR));
    }

    private boolean isSpeaking() {
        ModuleActivity activity = activityOf(TaiModule.TTS_PIPER);

        return activity == ModuleActivity.SYNTHESIZING || activity == ModuleActivity.SPEAKING;
    }

    private boolean isThinking() {
        return activityOf(TaiModule.LLM) == ModuleActivity.GENERATING
                || activityOf(TaiModule.STT_LISTENER) == ModuleActivity.PROCESSING
                || activityOf(TaiModule.STT_WHISPER) == ModuleActivity.PROCESSING;
    }

    private boolean isListening() {
        return activityOf(TaiModule.STT_LISTENER) == ModuleActivity.LISTENING
                || activityOf(TaiModule.STT_LISTENER) == ModuleActivity.CAPTURING;
    }

    private ModuleActivity activityOf(TaiModule module) {
        ModuleRuntimeSnapshot snapshot = registry.get(module);
        return snapshot == null ? null : snapshot.lastActivity();
    }
}
