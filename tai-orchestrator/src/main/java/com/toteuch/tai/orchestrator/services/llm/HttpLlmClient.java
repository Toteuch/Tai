package com.toteuch.tai.orchestrator.services.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(HttpLlmClient.class);

    private final RestClient restClient;

    public HttpLlmClient(
        @Value("${tai.llm.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public void generateReply(String correlationId, List<LlmMessage> messages) {
        try {
            var request = new LlmGenerationRequest(correlationId, messages);

            restClient.post()
                .uri("/llm/generate-reply")
                .body(request)
                .retrieve()
                .toBodilessEntity();

            log.info("LLM request sent | correlationId={}", correlationId);

        } catch (Exception e) {
            log.error("Failed to call LLM service | correlationId={}", correlationId, e);
        }
    }

    private record LlmGenerationRequest(
        String correlationId,
        List<LlmMessage> messages
    ) {
    }
}
