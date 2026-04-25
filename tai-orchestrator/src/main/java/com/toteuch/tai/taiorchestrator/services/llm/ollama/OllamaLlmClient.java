package com.toteuch.tai.taiorchestrator.services.llm.ollama;

import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.services.llm.ollama.dto.OllamaChatRequest;
import com.toteuch.tai.taiorchestrator.services.llm.ollama.dto.OllamaChatResponse;
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

import java.util.List;
import java.util.Map;

@Component
@Primary
public class OllamaLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaLlmClient.class);

    private final RestTemplate restTemplate;
    private final OllamaProperties ollamaProperties;

    public OllamaLlmClient(
        RestTemplate ollamaRestTemplate,
        OllamaProperties ollamaProperties
    ) {
        this.restTemplate = ollamaRestTemplate;
        this.ollamaProperties = ollamaProperties;
    }

    @Override
    public LlmGenerationResult generateReply(
        String sessionId,
        String correlationId,
        List<LlmMessage> messages
    ) {
        long start = System.currentTimeMillis();

        try {
            OllamaChatRequest request = buildRequest(messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OllamaChatRequest> entity = new HttpEntity<>(request, headers);

            log.info("Calling Ollama | sessionId={} correlationId={} model={} url={}",
                sessionId,
                correlationId,
                ollamaProperties.getModel(),
                ollamaProperties.getChatUrl());

            ResponseEntity<OllamaChatResponse> responseEntity = restTemplate.exchange(
                ollamaProperties.getChatUrl(),
                HttpMethod.POST,
                entity,
                OllamaChatResponse.class
            );

            long duration = System.currentTimeMillis() - start;
            OllamaChatResponse body = responseEntity.getBody();

            if (body == null) {
                return failure(
                    duration,
                    "OLLAMA_EMPTY_RESPONSE",
                    "Ollama returned an empty response body.",
                    false
                );
            }

            if (body.getMessage() == null || body.getMessage().getContent() == null || body.getMessage().getContent().isBlank()) {
                return failure(
                    duration,
                    "OLLAMA_EMPTY_MESSAGE",
                    "Ollama returned no assistant message content.",
                    false
                );
            }

            String content = body.getMessage().getContent().trim();

            log.info("Ollama reply received | sessionId={} correlationId={} model={} durationMs={}",
                sessionId,
                correlationId,
                body.getModel(),
                duration);

            return new LlmGenerationResult(
                true,
                content,
                body.getModel() != null ? body.getModel() : ollamaProperties.getModel(),
                body.getPromptEvalCount(),
                body.getEvalCount(),
                duration,
                null,
                null,
                false
            );

        } catch (HttpStatusCodeException e) {
            long duration = System.currentTimeMillis() - start;

            log.error("Ollama HTTP error | sessionId={} correlationId={} status={} body={}",
                sessionId,
                correlationId,
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e);

            return failure(
                duration,
                "OLLAMA_HTTP_ERROR",
                "Ollama returned HTTP " + e.getStatusCode().value() + ".",
                false
            );

        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - start;

            log.error("Ollama connection error | sessionId={} correlationId={}",
                sessionId,
                correlationId,
                e);

            return failure(
                duration,
                "OLLAMA_CONNECTION_ERROR",
                "Could not reach Ollama. Check that the local Ollama service is running.",
                true
            );

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            log.error("Unexpected Ollama error | sessionId={} correlationId={}",
                sessionId,
                correlationId,
                e);

            return failure(
                duration,
                "OLLAMA_UNEXPECTED_ERROR",
                e.getMessage() != null ? e.getMessage() : "Unexpected Ollama error.",
                false
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

    private LlmGenerationResult failure(
        long durationMs,
        String errorCode,
        String errorMessage,
        boolean retryable
    ) {
        return new LlmGenerationResult(
            false,
            null,
            ollamaProperties.getModel(),
            null,
            null,
            durationMs,
            errorCode,
            errorMessage,
            retryable
        );
    }
}
