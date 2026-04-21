package com.toteuch.tai.taiorchestrator.services.llm;

import com.toteuch.tai.taiorchestrator.events.inbound.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.LlmResponseFailedEvent;

import java.util.List;

public interface LlmClient {
    LlmGenerationResult generateReply(
        String sessionId,
        String correlationId,
        List<LlmMessage> messages
    );
}
