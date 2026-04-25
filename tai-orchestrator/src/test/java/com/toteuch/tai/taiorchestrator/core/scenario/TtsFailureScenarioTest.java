package com.toteuch.tai.taiorchestrator.core.scenario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class TtsFailureScenarioTest extends AbstractScenarioTest {

    @Test
    void should_complete_turn_when_tts_fails() {
        String correlationId = "tts-failure-1";
        String reply = "Hi there.";

        publishSttAccepted(correlationId, "Hello Tai");
        verify(llmClient).generateReply(eq(correlationId), anyList());

        publishLlmSuccess(correlationId, reply);
        verify(ttsClient).speak(correlationId, reply);

        publishTtsStarted(correlationId, reply);
        publishTtsFailed(correlationId);

        assertThat(sessionStore.get().getTurns()).hasSize(1);
        assertThat(sessionStore.get().getTurns().getFirst().getAssistantMessage()).isEqualTo(reply);
        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
