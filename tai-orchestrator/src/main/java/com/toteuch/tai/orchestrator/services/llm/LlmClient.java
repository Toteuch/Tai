package com.toteuch.tai.orchestrator.services.llm;

import java.util.List;

public interface LlmClient {
    void generateReply(String correlationId, List<LlmMessage> messages);
}
