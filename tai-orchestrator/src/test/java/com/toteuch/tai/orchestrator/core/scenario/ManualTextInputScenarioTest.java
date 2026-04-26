package com.toteuch.tai.orchestrator.core.scenario;

import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiManualTextInputReceivedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class ManualTextInputScenarioTest extends AbstractScenarioTest {

    @Test
    void should_process_manual_text_input_until_turn_is_completed() {
        String correlationId = UUID.randomUUID().toString();
        String userText = "Hello Tai";
        String reply = "Hi!";

        eventPublisher.publish(new UiManualTextInputReceivedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            correlationId,
            EventSource.UI,
            userText
        ));

        verify(llmClient).generateReply(eq(correlationId), anyList());
        publishLlmSuccess(correlationId, reply);

        verify(ttsClient).speak(correlationId, reply);

        publishTtsStarted(correlationId, reply);
        publishTtsCompleted(correlationId, reply);

        assertThat(sessionStore.get().getTurns()).hasSize(1);
        assertThat(sessionStore.get().getTurns().getFirst().getUserMessage()).isEqualTo(userText);
        assertThat(sessionStore.get().getTurns().getFirst().getAssistantMessage()).isEqualTo(reply);
        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
