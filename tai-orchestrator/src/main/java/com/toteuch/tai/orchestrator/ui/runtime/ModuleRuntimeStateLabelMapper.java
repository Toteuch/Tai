// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.runtime;

import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import org.springframework.stereotype.Component;

@Component
public class ModuleRuntimeStateLabelMapper {

    public String toStateLabel(ModuleRuntimeSnapshot snapshot) {
        if (snapshot == null) {
            return "Unknown";
        }

        if (snapshot.module() == TaiModule.SYSTEM) {
            return systemLabel(snapshot.health());
        }

        return toStateLabel(snapshot.module(), snapshot.lastActivity());
    }

    public String toStateLabel(ModuleActivity activity) {
        if (activity == null) {
            return "Unknown";
        }

        return switch (activity) {
            case UNKNOWN -> "Unknown";
            case DISABLED -> "Disabled";
            case IDLE -> "Idle";
            case LISTENING -> "Listening";
            case CAPTURING -> "Capturing";
            case PROCESSING -> "Processing";
            case GENERATING -> "Generating";
            case SYNTHESIZING -> "Synthesizing";
            case SPEAKING -> "Speaking";
            case ERROR -> "Error";
        };
    }

    public String toStateLabel(TaiModule module, ModuleActivity activity) {
        if (activity == null) {
            return "Unknown";
        }

        return switch (activity) {
            case UNKNOWN -> "Unknown";
            case DISABLED -> "Disabled";
            case ERROR -> "Error";
            case IDLE -> idleLabel(module);
            case LISTENING -> "Listening";
            case CAPTURING -> capturingLabel(module);
            case PROCESSING -> processingLabel(module);
            case GENERATING -> "Generating";
            case SYNTHESIZING -> "Synthesizing";
            case SPEAKING -> "Speaking";
        };
    }

    private String systemLabel(ModuleHealth health) {
        return switch (health) {
            case UP -> "Healthy";
            case DEGRADED -> "Degraded";
            case DOWN -> "Down";
            case DISABLED -> "Disabled";
        };
    }

    private String idleLabel(TaiModule module) {
        return switch (module) {
            case ORCHESTRATOR -> "Active";
            case TTS_PIPER -> "Silent";
            default -> "Idle";
        };
    }

    private String capturingLabel(TaiModule module) {
        return switch (module) {
            case STT_LISTENER -> "Recording";
            default -> "Capturing";
        };
    }

    private String processingLabel(TaiModule module) {
        return switch (module) {
            case STT_WHISPER -> "Transcribing";
            default -> "Processing";
        };
    }
}
