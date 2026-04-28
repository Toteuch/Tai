// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.transport;

import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.ollama.OllamaGenerationResult;
import com.toteuch.tai.llm.transport.dto.LlmResponseCompletedEventRequest;
import com.toteuch.tai.llm.transport.dto.LlmResponseFailedEventRequest;
import com.toteuch.tai.llm.transport.dto.TransportEventSource;
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
            LlmResponseCompletedEventRequest r = new LlmResponseCompletedEventRequest();
            r.setEventId(UUID.randomUUID().toString());
            r.setCreatedAt(Instant.now());
            r.setSource(TransportEventSource.LLM_SERVICE);
            r.setCorrelationId(correlationId);
            r.setResponseText(result.responseText());
            r.setModelName(result.modelName());
            r.setInputTokens(result.inputTokens());
            r.setOutputTokens(result.outputTokens());
            r.setGenerationDurationMs(result.generationDurationMs());
            client.post()
                    .uri(props.getOrchestrator().getCallbacks().getResponseCompletedPath())
                    .body(r)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.error("Failed to send LLM response completed event | {}", ex.getClass());
            throw ex;
        }
    }

    public void sendFailed(String correlationId, OllamaGenerationResult result) {
        try {
            LlmResponseFailedEventRequest r = new LlmResponseFailedEventRequest();
            r.setModelName(result.modelName());
            r.setEventId(UUID.randomUUID().toString());
            r.setCreatedAt(Instant.now());
            r.setSource(TransportEventSource.LLM_SERVICE);
            r.setCorrelationId(correlationId);
            r.setErrorCode(result.errorCode());
            r.setErrorMessage(result.errorMessage());
            client.post()
                    .uri(props.getOrchestrator().getCallbacks().getResponseFailedPath())
                    .body(r)
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
