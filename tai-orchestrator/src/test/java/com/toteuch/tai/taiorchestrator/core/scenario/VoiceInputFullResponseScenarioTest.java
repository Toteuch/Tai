package com.toteuch.tai.taiorchestrator.core.scenario;

import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SpeakingState;
import com.toteuch.tai.taiorchestrator.session.ThinkingState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class VoiceInputFullResponseScenarioTest extends AbstractScenarioTest {

    @Test
    void should_process_voice_input_until_turn_is_completed() {
        String correlationId = "voice-flow-1";
        String userText = "Hello Tai";
        String assistantReply = "Hi! It is good to talk to you again.";

        publishSttAccepted(correlationId, userText);

        verify(llmClient).generateReply(eq(correlationId), anyList());

        publishLlmSuccess(correlationId, assistantReply);

        verify(ttsClient).speak(correlationId, assistantReply);

        publishTtsStarted(correlationId, assistantReply);

        publishTtsCompleted(correlationId, assistantReply);

        SessionContext sessionContext = sessionStore.get();

        assertThat(sessionContext.getActiveTurn()).isNull();
        assertThat(sessionContext.getTurns()).hasSize(1);

        ConversationTurn turn = sessionContext.getTurns().getFirst();

        assertThat(turn.getCorrelationId()).isEqualTo(correlationId);
        assertThat(turn.getUserMessage()).isEqualTo(userText);
        assertThat(turn.getAssistantMessage()).isEqualTo(assistantReply);
        assertThat(turn.isAssistantReplyGenerated()).isTrue();
        assertThat(turn.isAssistantPlaybackStarted()).isTrue();
        assertThat(turn.isAssistantPlaybackCompleted()).isTrue();

        assertThat(sessionContext.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(sessionContext.getSpeakingState()).isEqualTo(SpeakingState.SILENT);

        ArgumentCaptor<List<LlmMessage>> contextCaptor = ArgumentCaptor.forClass(List.class);
        verify(llmClient).generateReply(eq(correlationId), contextCaptor.capture());

        List<LlmMessage> llmContext = contextCaptor.getValue();

        assertThat(llmContext).hasSize(2);
        assertThat(llmContext.get(0).role()).isEqualTo("system");
        assertThat(llmContext.get(1).role()).isEqualTo("user");
        assertThat(llmContext.get(1).content()).isEqualTo(userText);
    }
}
