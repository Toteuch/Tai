package com.toteuch.tai.orchestrator.services.llm;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpLlmClient implements LlmClient {

    private static final Logger traceLog = LoggerFactory.getLogger("tai.trace");
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");

    private final RestClient restClient;

    public HttpLlmClient(@Value("${tai.llm.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void generateReply(String correlationId, List<LlmMessage> messages) {
        try {
            var request = new LlmGenerationRequest(correlationId, messages);

            restClient
                    .post()
                    .uri("/llm/generate-reply")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            traceLog.trace("LLM request sent | correlationId={}", correlationId);

        } catch (Exception e) {
            errorLog.error("Failed to call LLM service | correlationId={}", correlationId, e);
        }
    }

    private record LlmGenerationRequest(String correlationId, List<LlmMessage> messages) {}
}
