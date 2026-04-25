package com.toteuch.tai.taiorchestrator.services.llm;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.LlmResponseFailedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
