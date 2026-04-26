package com.toteuch.tai.llm.service;

import com.toteuch.tai.llm.api.dto.LlmMessage;
import com.toteuch.tai.llm.config.LlmProperties;
import com.toteuch.tai.llm.ollama.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelWarmupService {
    private static final Logger log = LoggerFactory.getLogger(ModelWarmupService.class);
    private final LlmProperties props;
    private final OllamaClient client;
    private final ModelWarmupState state;

    public ModelWarmupService(LlmProperties props, OllamaClient client, ModelWarmupState state) {
        this.props = props;
        this.client = client;
        this.state = state;
    }

    @PostConstruct
    public void warmupOnStartup() {
        if (!props.getOllama().isWarmUpOnStartup()) return;
        try {
            OllamaGenerationResult r = client.generate(List.of(new LlmMessage("user", props.getOllama().getWarmUpPrompt())));
            if (r.success()) {
                state.markWarm();
                log.info("Ollama model warmed | model={}", props.getOllama().getModel());
            } else {
                state.markFailed(r.errorCode() + ": " + r.errorMessage());
                log.warn("Ollama model warmup failed | errorCode={} errorMessage={}", r.errorCode(), r.errorMessage());
            }
        } catch (Exception e) {
            state.markFailed(e.getMessage());
            log.warn("Ollama model warmup failed", e);
        }
    }
}
