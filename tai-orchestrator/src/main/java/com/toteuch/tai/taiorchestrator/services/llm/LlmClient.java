package com.toteuch.tai.taiorchestrator.services.llm;

import java.util.List;

public interface LlmClient {
    LlmGenerationResult generateReply(
        String correlationId,
        List<LlmMessage> messages
    );
}
