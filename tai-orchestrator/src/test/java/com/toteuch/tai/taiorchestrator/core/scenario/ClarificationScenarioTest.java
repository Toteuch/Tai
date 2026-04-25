package com.toteuch.tai.taiorchestrator.core.scenario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClarificationScenarioTest extends AbstractScenarioTest {

    @Test
    void should_generate_clarification_without_persisting_turn_in_history() {
        String correlationId = "clarification-1";
        String clarification = "Huh? Can you say that again?";

        when(llmClient.generateReply(eq(correlationId), anyList()))
            .thenReturn(llmSuccess(clarification));

        publishSttUnintelligible(correlationId);

        verify(ttsClient).speak(correlationId, clarification);

        publishTtsStarted(correlationId, clarification);
        publishTtsCompleted(correlationId, clarification);

        assertThat(sessionStore.get().getTurns()).isEmpty();
        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
