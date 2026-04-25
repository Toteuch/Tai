package com.toteuch.tai.taiorchestrator.services.llm.ollama;

import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.services.llm.ollama.dto.OllamaChatRequest;
import com.toteuch.tai.taiorchestrator.services.llm.ollama.dto.OllamaChatResponse;
import com.toteuch.tai.taiorchestrator.transport.LlmEventController;
import com.toteuch.tai.taiorchestrator.transport.events.TransportEventSource;
import com.toteuch.tai.taiorchestrator.transport.events.llm.LlmResponseCompletedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.llm.LlmResponseFailedEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Primary
public class OllamaLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaLlmClient.class);

    private final RestTemplate restTemplate;
    private final OllamaProperties ollamaProperties;
    private final LlmEventController llmEventController;

    public OllamaLlmClient(
        RestTemplate ollamaRestTemplate,
        OllamaProperties ollamaProperties,
        LlmEventController llmEventController
    ) {
        this.restTemplate = ollamaRestTemplate;
        this.ollamaProperties = ollamaProperties;
        this.llmEventController = llmEventController;
    }

    @Override
    public void generateReply(
        String correlationId,
        List<LlmMessage> messages
    ) {
        long start = System.currentTimeMillis();

        try {
            OllamaChatRequest request = buildRequest(messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OllamaChatRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Calling Ollama with {} messages in context | correlationId={} model={} url={}",
                messages.size(),
                correlationId,
                ollamaProperties.getModel(),
                ollamaProperties.getChatUrl());
            for (int i = 0; i < messages.size(); i++) {
                LlmMessage message = messages.get(i);
                if (message.role().equals("system") && message.content().startsWith("You are Tai.")) {
                    log.debug("SYSTEM PROMPT");
                }
                if (i > 0) log.debug(message.toString());
            }

            ResponseEntity<OllamaChatResponse> responseEntity = restTemplate.exchange(
                ollamaProperties.getChatUrl(),
                HttpMethod.POST,
                entity,
                OllamaChatResponse.class
            );

            long duration = System.currentTimeMillis() - start;
            OllamaChatResponse body = responseEntity.getBody();

            if (body == null) {
                postFailure(
                    correlationId,
                    "OLLAMA_EMPTY_RESPONSE",
                    "Ollama returned an empty response body.");
                return;
            }

            if (body.getMessage() == null || body.getMessage().getContent() == null || body.getMessage().getContent().isBlank()) {
                postFailure(
                    correlationId,
                    "OLLAMA_EMPTY_MESSAGE",
                    "Ollama returned no assistant message content."
                );
                return;
            }

            String content = body.getMessage().getContent().trim();

            log.debug("Ollama reply received | correlationId={} model={} durationMs={}",
                correlationId,
                body.getModel(),
                duration);

            postSuccess(
                correlationId,
                content,
                body.getPromptEvalCount(),
                body.getEvalCount(),
                duration
            );
        } catch (HttpStatusCodeException e) {
            long duration = System.currentTimeMillis() - start;

            log.error("Ollama HTTP error | correlationId={} status={} body={}",
                correlationId,
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e);

            postFailure(
                correlationId,
                "OLLAMA_HTTP_ERROR",
                "Ollama returned HTTP " + e.getStatusCode().value() + "."
            );
        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - start;

            log.error("Ollama connection error | correlationId={}",
                correlationId,
                e);

            postFailure(
                correlationId,
                "OLLAMA_CONNECTION_ERROR",
                "Could not reach Ollama. Check that the local Ollama service is running."
            );

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            log.error("Unexpected Ollama error | correlationId={}",
                correlationId,
                e);

            postFailure(
                correlationId,
                "OLLAMA_UNEXPECTED_ERROR",
                e.getMessage() != null ? e.getMessage() : "Unexpected Ollama error."
            );
        }
    }

    private OllamaChatRequest buildRequest(List<LlmMessage> messages) {
        List<OllamaChatRequest.MessageDto> messageDtos = messages.stream()
            .map(message -> new OllamaChatRequest.MessageDto(message.role(), message.content()))
            .toList();

        Map<String, Object> options = Map.of(
            "temperature", 0.7,
            "top_p", 0.9,
            "repeat_penalty", 1.1,
            "num_ctx", 8192,
            "num_predict", 256
        );

        return new OllamaChatRequest(
            ollamaProperties.getModel(),
            messageDtos,
            ollamaProperties.isStream(),
            ollamaProperties.getKeepAlive(),
            options
        );
    }

    private void postFailure(String correlationId, String errorCode, String errorMessage) {
        LlmResponseFailedEventRequest response = new LlmResponseFailedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setSource(TransportEventSource.LLM_SERVICE);
        response.setCorrelationId(correlationId);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);

        llmEventController.onResponseFailed(response);
    }

    private void postSuccess(
        String correlationId,
        String responseText,
        Integer inputTokens,
        Integer outputTokens,
        Long generationDurationMs
    ) {
        LlmResponseCompletedEventRequest response = new LlmResponseCompletedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setSource(TransportEventSource.LLM_SERVICE);
        response.setCorrelationId(correlationId);
        response.setResponseText(responseText);
        response.setModelName(ollamaProperties.getModel());
        response.setInputTokens(inputTokens);
        response.setOutputTokens(outputTokens);
        response.setGenerationDurationMs(generationDurationMs);

        llmEventController.onResponseCompleted(response);
    }
}
