package com.toteuch.tai.orchestrator.core.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class LlmFailureScenarioTest extends AbstractScenarioTest {

    @Test
    void should_complete_turn_without_tts_when_llm_fails() {
        String correlationId = "llm-failure-1";

        publishSttAccepted(correlationId, "Hello Tai");
        verify(llmClient).generateReply(eq(correlationId), anyList());
        publishLlmFailure(correlationId);

        verify(ttsClient, never()).speak(anyString(), anyString());

        assertThat(sessionStore.get().getTurns()).hasSize(1);
        assertThat(sessionStore.get().getTurns().getFirst().getCorrelationId())
                .isEqualTo(correlationId);
        assertThat(sessionStore.get().getTurns().getFirst().getAssistantMessage()).isNull();
        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
