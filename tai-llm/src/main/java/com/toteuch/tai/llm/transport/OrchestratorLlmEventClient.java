// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.transport;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.events.llm.LlmResponseFailedEvent;
import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.ollama.OllamaGenerationResult;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrchestratorLlmEventClient {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorLlmEventClient.class);
    private final RestClient client;
    private final LlmProperties props;

    public OrchestratorLlmEventClient(
            @Qualifier("orchestratorRestClient") RestClient orchestratorRestClient,
            LlmProperties props) {
        this.client = orchestratorRestClient;
        this.props = props;
    }

    public void sendCompleted(String correlationId, OllamaGenerationResult result) {
        try {
            LlmResponseCompletedEvent event =
                    new LlmResponseCompletedEvent(
                            UUID.randomUUID().toString(),
                            Instant.now(),
                            correlationId,
                            EventSource.LLM_SERVICE,
                            result.responseText(),
                            result.modelName(),
                            result.inputTokens(),
                            result.outputTokens(),
                            result.generationDurationMs());

            client.post()
                    .uri(props.getOrchestrator().getCallbacks().getResponseCompletedPath())
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.error("Failed to send LLM response completed event | {}", ex.getClass());
            throw ex;
        }
    }

    public void sendFailed(String correlationId, OllamaGenerationResult result) {
        try {
            LlmResponseFailedEvent event =
                    new LlmResponseFailedEvent(
                            UUID.randomUUID().toString(),
                            Instant.now(),
                            correlationId,
                            EventSource.LLM_SERVICE,
                            result.modelName(),
                            result.generationDurationMs(),
                            result.errorCode(),
                            result.errorMessage());

            client.post()
                    .uri(props.getOrchestrator().getCallbacks().getResponseFailedPath())
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send LLM response failed event | {}", ex.getClass());
            throw ex;
        }
    }

    public boolean isOrchestratorReachable() {
        try {
            client.get().uri("/actuator/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.debug("Orchestrator health check failed", e);
            return false;
        }
    }
}
