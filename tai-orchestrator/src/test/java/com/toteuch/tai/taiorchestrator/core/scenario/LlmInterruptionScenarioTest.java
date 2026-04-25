package com.toteuch.tai.taiorchestrator.core.scenario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LlmInterruptionScenarioTest extends AbstractScenarioTest {

    @Test
    void should_ignore_first_llm_response_when_second_user_input_supersedes_it() throws Exception {
        String firstCorrelationId = "llm-interrupt-1";
        String secondCorrelationId = "llm-interrupt-2";

        publishSttAccepted(firstCorrelationId, "First input");
        verify(llmClient).generateReply(eq(firstCorrelationId), anyList());

        publishSttAccepted(secondCorrelationId, "Second input");
        verify(llmClient).generateReply(eq(secondCorrelationId), anyList());

        publishLlmSuccess(firstCorrelationId, "First stale reply.");
        publishLlmSuccess(secondCorrelationId, "Second valid reply.");

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
