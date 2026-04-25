package com.toteuch.tai.taiorchestrator.core.scenario;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LlmInterruptionScenarioTest extends AbstractScenarioTest {

    @Test
    void should_ignore_first_llm_response_when_second_user_input_supersedes_it() throws Exception {
        String firstCorrelationId = "llm-interrupt-1";
        String secondCorrelationId = "llm-interrupt-2";

        CountDownLatch firstLlmStarted = new CountDownLatch(1);
        CountDownLatch allowFirstLlmToFinish = new CountDownLatch(1);

        when(llmClient.generateReply(eq(firstCorrelationId), anyList()))
            .thenAnswer(invocation -> {
                firstLlmStarted.countDown();
                allowFirstLlmToFinish.await();
                return llmSuccess("First stale reply.");
            });

        when(llmClient.generateReply(eq(secondCorrelationId), anyList()))
            .thenReturn(llmSuccess("Second valid reply."));

        Thread firstTurn = new Thread(() -> publishSttAccepted(firstCorrelationId, "First input"));
        firstTurn.start();

        firstLlmStarted.await();

        publishSttAccepted(secondCorrelationId, "Second input");

        allowFirstLlmToFinish.countDown();
        firstTurn.join();

        verify(ttsClient, never()).speak(eq(firstCorrelationId), anyString());
        verify(ttsClient).speak(eq(secondCorrelationId), eq("Second valid reply."));

        publishTtsStarted(secondCorrelationId, "Second valid reply.");
        publishTtsCompleted(secondCorrelationId, "Second valid reply.");

        assertThat(sessionStore.get().getTurns()).hasSize(2);
        assertThat(sessionStore.get().getTurns().get(0).getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(sessionStore.get().getTurns().get(0).getAssistantMessage()).isNull();
        assertThat(sessionStore.get().getTurns().get(1).getCorrelationId()).isEqualTo(secondCorrelationId);
        assertThat(sessionStore.get().getTurns().get(1).getAssistantMessage()).isEqualTo("Second valid reply.");
    }
}
