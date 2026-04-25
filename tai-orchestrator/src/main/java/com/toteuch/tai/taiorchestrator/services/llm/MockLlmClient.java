package com.toteuch.tai.taiorchestrator.services.llm;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockLlmClient implements LlmClient {

    @Override
    public LlmGenerationResult generateReply(
        String sessionId,
        String correlationId,
        List<LlmMessage> messages
    ) {
        try {
            String reply = "Hey Toteuch. I got your message. This is a mock reply from Tai.";
            return new LlmGenerationResult(
                true,
                reply,
                "mock-llm",
                null,
                null,
                50L,
                null,
                null,
                false
            );
        } catch (Exception e) {
            return new LlmGenerationResult(
                false,
                null,
                null,
                null,
                null,
                null,
                "LLM_ERROR",
                e.getMessage(),
                false
            );
        }
    }
}
