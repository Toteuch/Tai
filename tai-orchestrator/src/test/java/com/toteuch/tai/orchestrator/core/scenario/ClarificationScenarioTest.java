package com.toteuch.tai.orchestrator.core.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class ClarificationScenarioTest extends AbstractScenarioTest {

    @Test
    void should_generate_clarification_without_persisting_turn_in_history() {
        String correlationId = "clarification-1";
        String clarification = "Huh? Can you say that again?";

        publishSttUnintelligible(correlationId);
        verify(llmClient).generateReply(eq(correlationId), anyList());
        publishLlmSuccess(correlationId, clarification);

        verify(ttsClient).speak(correlationId, clarification);

        publishTtsStarted(correlationId, clarification);
        publishTtsCompleted(correlationId, clarification);

        assertThat(sessionStore.get().getTurns()).isEmpty();
        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
