package com.toteuch.tai.llm.service;

import com.toteuch.tai.llm.api.dto.LlmGenerationRequest;
import com.toteuch.tai.llm.ollama.*;
import com.toteuch.tai.llm.transport.OrchestratorLlmEventClient;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LlmGenerationService {
    private static final Logger log = LoggerFactory.getLogger(LlmGenerationService.class);
    private final OllamaClient ollama;
    private final OrchestratorLlmEventClient events;
    private final ModelWarmupState warmup;

    public LlmGenerationService(
            OllamaClient ollama, OrchestratorLlmEventClient events, ModelWarmupState warmup) {
        this.ollama = ollama;
        this.events = events;
        this.warmup = warmup;
    }

    @Async("llmTaskExecutor")
    public void generateReplyAsync(LlmGenerationRequest req) {
        log.info("LLM generation requested | correlationId={}", req.correlationId());
        OllamaGenerationResult r = ollama.generate(req.messages());
        if (r.success()) {
            warmup.markWarm();
            events.sendCompleted(req.correlationId(), r);
            log.info("LLM response callback sent | correlationId={}", req.correlationId());
        } else {
            warmup.markFailed(r.errorCode() + ": " + r.errorMessage());
            events.sendFailed(req.correlationId(), r);
            log.warn(
                    "LLM failure callback sent | correlationId={} errorCode={}",
                    req.correlationId(),
                    r.errorCode());
        }
    }
}
