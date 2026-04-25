package com.toteuch.tai.taiorchestrator.core.scenario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TtsInterruptionScenarioTest extends AbstractScenarioTest {

    @Test
    void should_stop_current_tts_and_process_new_user_input() {
        String firstCorrelationId = "tts-interrupt-1";
        String secondCorrelationId = "tts-interrupt-2";

        when(llmClient.generateReply(eq(firstCorrelationId), anyList()))
            .thenReturn(llmSuccess("First reply."));
        when(llmClient.generateReply(eq(secondCorrelationId), anyList()))
            .thenReturn(llmSuccess("Second reply."));

        publishSttAccepted(firstCorrelationId, "First input");

        verify(ttsClient).speak(firstCorrelationId, "First reply.");

        publishTtsStarted(firstCorrelationId, "First reply.");

        publishSttAccepted(secondCorrelationId, "Second input");

        verify(ttsClient).stop(firstCorrelationId);
        verify(ttsClient).speak(secondCorrelationId, "Second reply.");

        publishTtsStarted(secondCorrelationId, "Second reply.");
        publishTtsCompleted(secondCorrelationId, "Second reply.");

        assertThat(sessionStore.get().getTurns()).anySatisfy(turn -> {
            assertThat(turn.getCorrelationId()).isEqualTo(firstCorrelationId);
            assertThat(turn.isAssistantPlaybackInterrupted()).isTrue();
        });

        assertThat(sessionStore.get().getTurns()).anySatisfy(turn -> {
            assertThat(turn.getCorrelationId()).isEqualTo(secondCorrelationId);
            assertThat(turn.getAssistantMessage()).isEqualTo("Second reply.");
        });

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
