package com.toteuch.tai.taiorchestrator.services.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlowMockLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(SlowMockLlmClient.class);

    private final long delayMs;

    public SlowMockLlmClient(@Value("${tai.mock.llm.delay-ms:4000}") long delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public LlmGenerationResult generateReply(
        String sessionId,
        String correlationId,
        List<LlmMessage> messages
    ) {
        long start = System.currentTimeMillis();

        try {
            log.info("SlowMockLlmClient started | sessionId={} correlationId={} delayMs={}",
                sessionId, correlationId, delayMs);

            Thread.sleep(delayMs);

            String lastUserMessage = messages.isEmpty()
                ? "I did not receive any message."
                : messages.get(messages.size() - 1).content();

            String reply = "Hey Toteuch. I got your message: " + lastUserMessage +
                ". This is a slow mock reply from Tai.";

            long duration = System.currentTimeMillis() - start;

            log.info("SlowMockLlmClient completed | sessionId={} correlationId={} durationMs={}",
                sessionId, correlationId, duration);

            return new LlmGenerationResult(
                true,
                reply,
                "slow-mock-llm",
                null,
                null,
                duration,
                null,
                null,
                false
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            long duration = System.currentTimeMillis() - start;

            log.warn("SlowMockLlmClient interrupted | sessionId={} correlationId={} durationMs={}",
                sessionId, correlationId, duration);

            return new LlmGenerationResult(
                false,
                null,
                "slow-mock-llm",
                null,
                null,
                duration,
                "LLM_INTERRUPTED",
                "Slow mock LLM generation was interrupted.",
                true
            );
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            log.error("SlowMockLlmClient failed | sessionId={} correlationId={} durationMs={}",
                sessionId, correlationId, duration, e);

            return new LlmGenerationResult(
                false,
                null,
                "slow-mock-llm",
                null,
                null,
                duration,
                "LLM_ERROR",
                e.getMessage(),
                false
            );
        }
    }
}
