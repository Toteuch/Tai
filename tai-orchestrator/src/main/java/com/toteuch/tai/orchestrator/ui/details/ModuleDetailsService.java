// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.details;

import com.toteuch.tai.orchestrator.ui.model.ModuleDetails;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeStateLabelMapper;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleDetailsService {

    private final ModuleRuntimeRegistry registry;
    private final ModuleRuntimeStateLabelMapper stateLabelMapper;

    public ModuleDetailsService(
            ModuleRuntimeRegistry registry, ModuleRuntimeStateLabelMapper stateLabelMapper) {
        this.registry = registry;
        this.stateLabelMapper = stateLabelMapper;
    }

    public ModuleDetails getDetails(TaiModule module) {
        ModuleRuntimeSnapshot snapshot = registry.get(module);

        if (snapshot == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Unknown Tai module: " + module);
        }

        Map<String, Object> details = snapshot.details();

        return new ModuleDetails(
                snapshot.module(),
                snapshot.health(),
                stateLabelMapper.toStateLabel(snapshot.lastActivity()),
                checkedAt(snapshot),
                snapshot.lastActiveCorrelationId(),
                extractLastProcessTimeMs(details),
                snapshot.lastError(),
                details);
    }

    private Instant checkedAt(ModuleRuntimeSnapshot snapshot) {
        if (snapshot.lastHealthAt() != null) {
            return snapshot.lastHealthAt();
        }

        return snapshot.lastActivityAt();
    }

    private Long extractLastProcessTimeMs(Map<String, Object> details) {
        Object value = details.get("lastProcessTimeMs");

        if (value instanceof Number number) {
            return number.longValue();
        }

        Object responseTimeMs = details.get("responseTimeMs");

        if (responseTimeMs instanceof Number number) {
            return number.longValue();
        }

        return null;
    }
}
