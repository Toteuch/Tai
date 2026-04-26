package com.toteuch.tai.llm.transport;

import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.ollama.OllamaGenerationResult;
import com.toteuch.tai.llm.transport.dto.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrchestratorLlmEventClient {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorLlmEventClient.class);
    private final RestClient client;
    private final LlmProperties props;

    public OrchestratorLlmEventClient(RestClient orchestratorRestClient, LlmProperties props) {
        this.client = orchestratorRestClient;
        this.props = props;
    }

    public void sendCompleted(String correlationId, OllamaGenerationResult result) {
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
        client.post().uri(props.getOrchestrator().getCallbacks().getResponseCompletedPath()).body(r).retrieve().toBodilessEntity();
    }

    public void sendFailed(String correlationId, OllamaGenerationResult result) {
        LlmResponseFailedEventRequest r = new LlmResponseFailedEventRequest();
        r.setEventId(UUID.randomUUID().toString());
        r.setCreatedAt(Instant.now());
        r.setSource(TransportEventSource.LLM_SERVICE);
        r.setCorrelationId(correlationId);
        r.setErrorCode(result.errorCode());
        r.setErrorMessage(result.errorMessage());
        client.post().uri(props.getOrchestrator().getCallbacks().getResponseFailedPath()).body(r).retrieve().toBodilessEntity();
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
