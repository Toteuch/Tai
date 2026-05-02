// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.toteuch.tai.orchestrator.AbstractScenarioTest;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import org.junit.jupiter.api.Test;

class BargeInScenarioTest extends AbstractScenarioTest {

    @Test
    void should_handle_barge_in_during_tts_speech_from_stt_unintelligible_event() {
        String firstCorrelationId = "barge-tts-speech-1";
        String clarificationCorrelationId = "barge-tts-speech-clarification";

        publishSttSpeechStarted(firstCorrelationId);
        publishSttAccepted(firstCorrelationId, "First input");
        publishLlmSuccess(firstCorrelationId, "First reply.");
        publishTtsStarted(firstCorrelationId, "First reply.");

        publishSttSpeechStarted(clarificationCorrelationId);
        publishUserSpeechStarted(clarificationCorrelationId);
        verify(ttsClient).stop(firstCorrelationId);

        publishSttUnintelligible(clarificationCorrelationId);
        publishLlmSuccess(clarificationCorrelationId, "Can you say that again?");

        verify(ttsClient).speak(clarificationCorrelationId, "Can you say that again?");

        publishTtsStarted(clarificationCorrelationId, "Can you say that again?");
        publishTtsCompleted(clarificationCorrelationId, "Can you say that again?");

        assertThat(sessionStore.get().getTurns()).hasSize(1);

        ConversationTurn interruptedTurn = sessionStore.get().getTurns().get(0);
        assertThat(interruptedTurn.getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(interruptedTurn.isAssistantPlaybackStarted()).isTrue();
        assertThat(interruptedTurn.isAssistantPlaybackInterrupted()).isTrue();

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }

    @Test
    void should_handle_barge_in_during_tts_speech_from_stt_accepted_event() {
        String firstCorrelationId = "barge-tts-speech-1";
        String secondCorrelationId = "barge-tts-speech-2";

        publishSttSpeechStarted(firstCorrelationId);
        publishSttAccepted(firstCorrelationId, "First input");
        publishLlmSuccess(firstCorrelationId, "First reply.");
        publishTtsStarted(firstCorrelationId, "First reply.");

        publishSttSpeechStarted(secondCorrelationId);
        publishUserSpeechStarted(secondCorrelationId);
        verify(ttsClient).stop(firstCorrelationId);

        publishSttAccepted(secondCorrelationId, "Second input");
        publishLlmSuccess(secondCorrelationId, "Second reply.");
        verify(ttsClient).speak(secondCorrelationId, "Second reply.");

        publishTtsStarted(secondCorrelationId, "Second reply.");
        publishTtsCompleted(secondCorrelationId, "Second reply.");

        assertThat(sessionStore.get().getTurns()).hasSize(2);

        ConversationTurn interruptedTurn = sessionStore.get().getTurns().get(0);
        assertThat(interruptedTurn.getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(interruptedTurn.isAssistantPlaybackStarted()).isTrue();
        assertThat(interruptedTurn.isAssistantPlaybackInterrupted()).isTrue();

        ConversationTurn secondTurn = sessionStore.get().getTurns().get(1);
        assertThat(secondTurn.getCorrelationId()).isEqualTo(secondCorrelationId);
        assertThat(secondTurn.getAssistantMessage()).isEqualTo("Second reply.");

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }

    @Test
    void should_handle_barge_in_during_llm_generation_from_stt_unintelligible_event()
            throws Exception {
        String firstCorrelationId = "barge-llm-1";
        String clarificationCorrelationId = "barge-llm-clarification";

        publishSttSpeechStarted(firstCorrelationId);
        verify(ttsClient, never()).stop(anyString());
        publishSttAccepted(firstCorrelationId, "First input");
        verify(llmClient).generateReply(eq(firstCorrelationId), anyList());

        publishSttSpeechStarted(clarificationCorrelationId);
        verify(ttsClient, never()).stop(anyString());
        publishSttUnintelligible(clarificationCorrelationId);
        verify(llmClient).generateReply(eq(clarificationCorrelationId), anyList());

        publishLlmSuccess(firstCorrelationId, "Late stale reply.");
        publishLlmSuccess(clarificationCorrelationId, "Can you repeat that?");

        verify(ttsClient, never()).speak(eq(firstCorrelationId), anyString());
        verify(ttsClient).speak(clarificationCorrelationId, "Can you repeat that?");

        publishTtsStarted(clarificationCorrelationId, "Can you repeat that?");
        publishTtsCompleted(clarificationCorrelationId, "Can you repeat that?");

        assertThat(sessionStore.get().getTurns()).hasSize(1);

        ConversationTurn firstTurnInHistory = sessionStore.get().getTurns().get(0);
        assertThat(firstTurnInHistory.getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(firstTurnInHistory.getUserMessage()).isEqualTo("First input");
        assertThat(firstTurnInHistory.getAssistantMessage()).isNull();
        assertThat(firstTurnInHistory.isSupersededBeforeAssistantReply()).isTrue();

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }

    @Test
    void should_handle_barge_in_during_llm_generation_from_stt_accepted_event() throws Exception {
        String firstCorrelationId = "barge-llm-1";
        String secondCorrelationId = "barge-llm-2";

        publishSttSpeechStarted(firstCorrelationId);
        verify(ttsClient, never()).stop(anyString());
        publishSttAccepted(firstCorrelationId, "First input");
        verify(llmClient).generateReply(eq(firstCorrelationId), anyList());

        publishSttSpeechStarted(secondCorrelationId);
        verify(ttsClient, never()).stop(anyString());
        publishSttAccepted(secondCorrelationId, "Second input");
        verify(llmClient).generateReply(eq(secondCorrelationId), anyList());

        publishLlmSuccess(firstCorrelationId, "Late stale reply.");
        publishLlmSuccess(secondCorrelationId, "Second reply.");

        verify(ttsClient, never()).speak(eq(firstCorrelationId), anyString());
        verify(ttsClient).speak(secondCorrelationId, "Second reply.");

        publishTtsStarted(secondCorrelationId, "Second reply.");
        publishTtsCompleted(secondCorrelationId, "Second reply.");

        assertThat(sessionStore.get().getTurns()).hasSize(2);

        ConversationTurn firstTurnInHistory = sessionStore.get().getTurns().get(0);
        assertThat(firstTurnInHistory.getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(firstTurnInHistory.getUserMessage()).isEqualTo("First input");
        assertThat(firstTurnInHistory.getAssistantMessage()).isNull();
        assertThat(firstTurnInHistory.isSupersededBeforeAssistantReply()).isTrue();

        ConversationTurn secondTurn = sessionStore.get().getTurns().get(1);
        assertThat(secondTurn.getCorrelationId()).isEqualTo(secondCorrelationId);
        assertThat(secondTurn.getAssistantMessage()).isEqualTo("Second reply.");

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }

    @Test
    void should_handle_barge_in_during_tts_preparing_from_stt_unintelligible_event() {
        String firstCorrelationId = "barge-tts-preparing-1";
        String clarificationCorrelationId = "barge-tts-preparing-clarification";

        publishSttSpeechStarted(firstCorrelationId);
        verify(ttsClient, never()).stop(anyString());
        publishSttAccepted(firstCorrelationId, "First input");
        verify(llmClient).generateReply(eq(firstCorrelationId), anyList());
        publishLlmSuccess(firstCorrelationId, "First reply.");
        verify(ttsClient).speak(firstCorrelationId, "First reply.");

        publishSttSpeechStarted(clarificationCorrelationId);
        verify(ttsClient).stop(firstCorrelationId);
        publishSttUnintelligible(clarificationCorrelationId);
        verify(llmClient).generateReply(eq(clarificationCorrelationId), anyList());
        publishLlmSuccess(clarificationCorrelationId, "Say that again?");
        verify(ttsClient).speak(clarificationCorrelationId, "Say that again?");

        publishTtsStarted(clarificationCorrelationId, "Say that again?");
        publishTtsCompleted(clarificationCorrelationId, "Say that again?");

        assertThat(sessionStore.get().getTurns()).hasSize(1);

        ConversationTurn interruptedTurn = sessionStore.get().getTurns().get(0);
        assertThat(interruptedTurn.getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(interruptedTurn.getAssistantMessage()).isEqualTo("First reply.");
        assertThat(interruptedTurn.isAssistantPlaybackStarted()).isFalse();
        assertThat(interruptedTurn.isAssistantPlaybackInterrupted()).isTrue();

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }

    @Test
    void should_handle_barge_in_during_tts_preparing_from_stt_accepted_event() {
        String firstCorrelationId = "barge-tts-preparing-1";
        String secondCorrelationId = "barge-tts-preparing-2";

        publishSttSpeechStarted(firstCorrelationId);
        verify(ttsClient, never()).stop(anyString());
        publishSttAccepted(firstCorrelationId, "First input");
        verify(llmClient).generateReply(eq(firstCorrelationId), anyList());
        publishLlmSuccess(firstCorrelationId, "First reply.");
        verify(ttsClient).speak(firstCorrelationId, "First reply.");

        publishSttSpeechStarted(secondCorrelationId);
        verify(ttsClient).stop(firstCorrelationId);
        publishSttAccepted(secondCorrelationId, "Second input");
        verify(llmClient).generateReply(eq(secondCorrelationId), anyList());
        publishLlmSuccess(secondCorrelationId, "Second reply.");
        verify(ttsClient).speak(secondCorrelationId, "Second reply.");

        publishTtsStarted(secondCorrelationId, "Second reply.");
        publishTtsCompleted(secondCorrelationId, "Second reply.");

        assertThat(sessionStore.get().getTurns()).hasSize(2);

        ConversationTurn interruptedTurn = sessionStore.get().getTurns().get(0);
        assertThat(interruptedTurn.getCorrelationId()).isEqualTo(firstCorrelationId);
        assertThat(interruptedTurn.getAssistantMessage()).isEqualTo("First reply.");
        assertThat(interruptedTurn.isAssistantPlaybackStarted()).isFalse();
        assertThat(interruptedTurn.isAssistantPlaybackInterrupted()).isTrue();

        ConversationTurn secondTurn = sessionStore.get().getTurns().get(1);
        assertThat(secondTurn.getCorrelationId()).isEqualTo(secondCorrelationId);
        assertThat(secondTurn.getAssistantMessage()).isEqualTo("Second reply.");

        assertThat(sessionStore.get().getActiveTurn()).isNull();
    }
}
