// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.ollama;

import com.toteuch.tai.llm.api.dto.LlmMessage;
import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.ollama.dto.OllamaChatRequest;
import com.toteuch.tai.llm.ollama.dto.OllamaChatResponse;
import com.toteuch.tai.llm.ollama.dto.OllamaMessage;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OllamaClient {
    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private final RestClient client;
    private final LlmProperties props;

    public OllamaClient(
            @Qualifier("ollamaRestClient") RestClient ollamaRestClient, LlmProperties props) {
        this.client = ollamaRestClient;
        this.props = props;
    }

    public OllamaGenerationResult generate(List<LlmMessage> messages) {
        Instant start = Instant.now();
        try {
            OllamaChatRequest req =
                    new OllamaChatRequest(
                            props.getOllama().getModel(),
                            messages.stream()
                                    .map(m -> new OllamaMessage(m.role(), m.content()))
                                    .toList(),
                            props.getOllama().isStream(),
                            props.getOllama().getKeepAlive());
            OllamaChatResponse res =
                    client.post()
                            .uri(props.getOllama().getChatPath())
                            .body(req)
                            .retrieve()
                            .body(OllamaChatResponse.class);
            long ms = Duration.between(start, Instant.now()).toMillis();
            if (res == null)
                return OllamaGenerationResult.failure(
                        props.getOllama().getModel(),
                        "OLLAMA_EMPTY_RESPONSE",
                        "Ollama returned an empty response.",
                        ms);
            if (res.getMessage() == null
                    || res.getMessage().content() == null
                    || res.getMessage().content().isBlank())
                return OllamaGenerationResult.failure(
                        props.getOllama().getModel(),
                        "OLLAMA_EMPTY_MESSAGE",
                        "Ollama returned no assistant message content.",
                        ms);
            return OllamaGenerationResult.success(
                    res.getMessage().content(),
                    res.getModel() != null ? res.getModel() : props.getOllama().getModel(),
                    res.getPrompt_eval_count(),
                    res.getEval_count(),
                    ms);
        } catch (RestClientException e) {
            long ms = Duration.between(start, Instant.now()).toMillis();
            log.warn("Ollama call failed", e);
            return OllamaGenerationResult.failure(
                    props.getOllama().getModel(), "OLLAMA_HTTP_ERROR", e.getMessage(), ms);
        } catch (Exception e) {
            long ms = Duration.between(start, Instant.now()).toMillis();
            log.warn("Ollama generation failed", e);
            return OllamaGenerationResult.failure(
                    props.getOllama().getModel(), "LLM_INTERNAL_ERROR", e.getMessage(), ms);
        }
    }

    public boolean isReachable() {
        try {
            client.get().uri(props.getOllama().getTagsPath()).retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
