package com.toteuch.tai.orchestrator.core.scenario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

class NoiseScenarioTest extends AbstractScenarioTest {

    @Test
    void should_ignore_noise_without_calling_llm_or_tts() {
        publishSttNoise("noise-1");

        verifyNoInteractions(llmClient);
        verifyNoInteractions(ttsClient);

        assertThat(sessionStore.get().getTurns()).isEmpty();
        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
