// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.health;

import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.service.ModelWarmupState;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("modelWarmup")
public class ModelWarmupHealthIndicator implements HealthIndicator {
    private final ModelWarmupState state;
    private final LlmProperties props;

    public ModelWarmupHealthIndicator(ModelWarmupState state, LlmProperties props) {
        this.state = state;
        this.props = props;
    }

    @Override
    public Health health() {
        Health.Builder b = state.isWarm() ? Health.up() : Health.status("DEGRADED");
        return b.withDetail("model", safeGetOnState(props.getOllama().getModel()))
                .withDetail("warm", state.isWarm())
                .withDetail("lastWarmupAt", safeGetOnState(state.getLastWarmupAt()))
                .withDetail("lastError", safeGetOnState(state.getLastError()))
                .build();
    }

    private Object safeGetOnState(Object o) {
        if (o == null) {
            o = "";
        }
        return o;
    }
}
