// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.health;

import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.ollama.OllamaClient;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("ollama")
public class OllamaHealthIndicator implements HealthIndicator {
    private final OllamaClient client;
    private final LlmProperties props;

    public OllamaHealthIndicator(OllamaClient client, LlmProperties props) {
        this.client = client;
        this.props = props;
    }

    @Override
    public Health health() {
        if (client.isReachable())
            return Health.up()
                    .withDetail("baseUrl", props.getOllama().getBaseUrl())
                    .withDetail("model", props.getOllama().getModel())
                    .build();
        return Health.down()
                .withDetail("baseUrl", props.getOllama().getBaseUrl())
                .withDetail("model", props.getOllama().getModel())
                .build();
    }
}
