// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.AbstractScenarioTest;
import com.toteuch.tai.orchestrator.ui.model.ConversationStatus;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.ModuleOverview;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import com.toteuch.tai.orchestrator.ui.model.Utterance;
import com.toteuch.tai.orchestrator.ui.model.UtteranceRole;
import com.toteuch.tai.orchestrator.ui.model.UtteranceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaiUiStateProjectionScenarioTest extends AbstractScenarioTest {

    @Autowired private TaiUiStateProjectionService taiUiStateProjectionService;

    @Test
    void nominal_voice_flow_should_project_expected_ui_state_after_each_inbound_event() {
        String correlationId = "turn-nominal";

        publishSttSpeechStarted(correlationId);

        TaiUiState afterSpeechStarted = project();

        assertThat(afterSpeechStarted.sequence()).isEqualTo(1L);
        assertThat(afterSpeechStarted.schemaVersion()).isEqualTo("2.0");
        assertThat(afterSpeechStarted.generatedAt()).isNotNull();
        assertThat(afterSpeechStarted.conversationStatus()).isEqualTo(ConversationStatus.LISTENING);
        assertModule(afterSpeechStarted, TaiModule.STT_LISTENER, ModuleHealth.UP, "Recording");
        assertThat(afterSpeechStarted.lastUserUtterance()).isNull();
        assertThat(afterSpeechStarted.lastAssistantUtterance()).isNull();

        publishSttAccepted(correlationId, "Hello Tai");

        TaiUiState afterSttAccepted = project();

        assertThat(afterSttAccepted.sequence()).isEqualTo(2L);
        assertThat(afterSttAccepted.conversationStatus()).isEqualTo(ConversationStatus.THINKING);
        assertModule(afterSttAccepted, TaiModule.STT_LISTENER, ModuleHealth.UP, "Listening");
        assertModule(afterSttAccepted, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertUtterance(
                afterSttAccepted.lastUserUtterance(),
                UtteranceRole.USER,
                "Hello Tai",
                correlationId,
                UtteranceStatus.COMPLETED);
        assertThat(afterSttAccepted.lastAssistantUtterance()).isNull();

        publishLlmSuccess(correlationId, "Hello. How can I help?");

        TaiUiState afterLlmSuccess = project();

        assertThat(afterLlmSuccess.sequence()).isEqualTo(3L);
        assertThat(afterLlmSuccess.conversationStatus()).isEqualTo(ConversationStatus.SPEAKING);
        assertModule(afterLlmSuccess, TaiModule.LLM, ModuleHealth.UP, "Idle");
        assertModule(afterLlmSuccess, TaiModule.TTS_PIPER, ModuleHealth.UP, "Synthesizing");
        assertUtterance(
                afterLlmSuccess.lastUserUtterance(),
                UtteranceRole.USER,
                "Hello Tai",
                correlationId,
                UtteranceStatus.COMPLETED);
        assertUtterance(
                afterLlmSuccess.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "Hello. How can I help?",
                correlationId,
                UtteranceStatus.STARTED);

        publishTtsStarted(correlationId, "Hello. How can I help?");

        TaiUiState afterTtsStarted = project();

        assertThat(afterTtsStarted.sequence()).isEqualTo(4L);
        assertThat(afterTtsStarted.conversationStatus()).isEqualTo(ConversationStatus.SPEAKING);
        assertModule(afterTtsStarted, TaiModule.TTS_PIPER, ModuleHealth.UP, "Speaking");
        assertUtterance(
                afterTtsStarted.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "Hello. How can I help?",
                correlationId,
                UtteranceStatus.STARTED);

        publishTtsCompleted(correlationId, "Hello. How can I help?");

        TaiUiState afterTtsCompleted = project();

        assertThat(afterTtsCompleted.sequence()).isEqualTo(5L);
        assertThat(afterTtsCompleted.conversationStatus()).isEqualTo(ConversationStatus.LISTENING);
        assertModule(afterTtsCompleted, TaiModule.TTS_PIPER, ModuleHealth.UP, "Silent");
        assertUtterance(
                afterTtsCompleted.lastUserUtterance(),
                UtteranceRole.USER,
                "Hello Tai",
                correlationId,
                UtteranceStatus.COMPLETED);
        assertUtterance(
                afterTtsCompleted.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "Hello. How can I help?",
                correlationId,
                UtteranceStatus.COMPLETED);
    }

    @Test
    void llm_failure_flow_should_project_error_state_and_last_assistant_utterance() {
        String succeedCorrelationId = "succeed-corr-id";
        String correlationId = "turn-llm-failure";

        publishSttSpeechStarted(succeedCorrelationId);
        publishSttAccepted(succeedCorrelationId, "Hello Tai");
        publishLlmSuccess(succeedCorrelationId, "Hi Toteuch !");
        publishTtsStarted(succeedCorrelationId, "Hi Toteuch !");
        publishTtsCompleted(succeedCorrelationId, "Hi Toteuch !");
        project();

        publishSttSpeechStarted(correlationId);
        project();

        publishSttAccepted(correlationId, "Tell me something");

        TaiUiState afterSttAccepted = project();

        assertThat(afterSttAccepted.conversationStatus()).isEqualTo(ConversationStatus.THINKING);
        assertModule(afterSttAccepted, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertUtterance(
                afterSttAccepted.lastUserUtterance(),
                UtteranceRole.USER,
                "Tell me something",
                correlationId,
                UtteranceStatus.COMPLETED);
        assertThat(afterSttAccepted.lastAssistantUtterance()).isNotNull();
        assertThat(afterSttAccepted.lastAssistantUtterance().correlationId())
                .isEqualTo(succeedCorrelationId);

        publishLlmFailure(correlationId);

        TaiUiState afterLlmFailure = project();

        assertThat(afterLlmFailure.conversationStatus()).isEqualTo(ConversationStatus.ERROR);
        assertModule(afterLlmFailure, TaiModule.LLM, ModuleHealth.DEGRADED, "Error");
        assertUtterance(
                afterLlmFailure.lastUserUtterance(),
                UtteranceRole.USER,
                "Tell me something",
                correlationId,
                UtteranceStatus.COMPLETED);

        Utterance assistant = afterLlmFailure.lastAssistantUtterance();
        assertThat(assistant).isNotNull();
        assertThat(assistant.role()).isEqualTo(UtteranceRole.ASSISTANT);
        assertThat(assistant.correlationId()).isEqualTo(succeedCorrelationId);
        assertThat(assistant.status()).isEqualTo(UtteranceStatus.COMPLETED);
    }

    @Test
    void tts_failure_flow_should_project_error_state_and_failed_assistant_utterance_with_text() {
        String correlationId = "turn-tts-failure";

        publishSttSpeechStarted(correlationId);
        project();

        publishSttAccepted(correlationId, "Say something");
        project();

        publishLlmSuccess(correlationId, "Something");

        TaiUiState afterLlmSuccess = project();

        assertThat(afterLlmSuccess.conversationStatus()).isEqualTo(ConversationStatus.SPEAKING);
        assertModule(afterLlmSuccess, TaiModule.TTS_PIPER, ModuleHealth.UP, "Synthesizing");
        assertUtterance(
                afterLlmSuccess.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "Something",
                correlationId,
                UtteranceStatus.STARTED);

        publishTtsFailed(correlationId);

        TaiUiState afterTtsFailed = project();

        assertThat(afterTtsFailed.conversationStatus()).isEqualTo(ConversationStatus.ERROR);
        assertModule(afterTtsFailed, TaiModule.TTS_PIPER, ModuleHealth.DEGRADED, "Error");
        assertUtterance(
                afterTtsFailed.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "Something",
                correlationId,
                UtteranceStatus.FAILED);
    }

    @Test
    void stt_noise_flow_should_not_project_user_or_assistant_utterance() {
        String correlationId = "turn-stt-noise";

        publishSttSpeechStarted(correlationId);

        TaiUiState afterSpeechStarted = project();

        assertThat(afterSpeechStarted.conversationStatus()).isEqualTo(ConversationStatus.LISTENING);
        assertModule(afterSpeechStarted, TaiModule.STT_LISTENER, ModuleHealth.UP, "Recording");
        assertThat(afterSpeechStarted.lastUserUtterance()).isNull();
        assertThat(afterSpeechStarted.lastAssistantUtterance()).isNull();

        publishSttNoise(correlationId);

        TaiUiState afterNoise = project();

        assertThat(afterNoise.conversationStatus()).isEqualTo(ConversationStatus.LISTENING);
        assertModule(afterNoise, TaiModule.STT_LISTENER, ModuleHealth.UP, "Listening");
        assertThat(afterNoise.lastUserUtterance()).isNull();
        assertThat(afterNoise.lastAssistantUtterance()).isNull();
    }

    @Test
    void
            barge_in_while_llm_is_generating_should_project_new_turn_and_ignore_late_stale_llm_response() {
        String firstCorrelationId = "turn-first";
        String secondCorrelationId = "turn-second";

        publishSttSpeechStarted(firstCorrelationId);
        project();

        publishSttAccepted(firstCorrelationId, "First question");

        TaiUiState afterFirstAccepted = project();

        assertThat(afterFirstAccepted.conversationStatus()).isEqualTo(ConversationStatus.THINKING);
        assertModule(afterFirstAccepted, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertUtterance(
                afterFirstAccepted.lastUserUtterance(),
                UtteranceRole.USER,
                "First question",
                firstCorrelationId,
                UtteranceStatus.COMPLETED);

        publishSttSpeechStarted(secondCorrelationId);

        TaiUiState afterSecondSpeechStarted = project();

        /*
         * Ollama generation cannot be cancelled, so LLM remains generating for the first turn
         * until the second accepted transcript starts the next generation.
         */
        assertThat(afterSecondSpeechStarted.conversationStatus())
                .isEqualTo(ConversationStatus.THINKING);
        assertModule(
                afterSecondSpeechStarted, TaiModule.STT_LISTENER, ModuleHealth.UP, "Recording");
        assertModule(afterSecondSpeechStarted, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertUtterance(
                afterSecondSpeechStarted.lastUserUtterance(),
                UtteranceRole.USER,
                "First question",
                firstCorrelationId,
                UtteranceStatus.COMPLETED);

        publishSttAccepted(secondCorrelationId, "Second question");

        TaiUiState afterSecondAccepted = project();

        assertThat(afterSecondAccepted.conversationStatus()).isEqualTo(ConversationStatus.THINKING);
        assertModule(afterSecondAccepted, TaiModule.STT_LISTENER, ModuleHealth.UP, "Listening");
        assertModule(afterSecondAccepted, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertUtterance(
                afterSecondAccepted.lastUserUtterance(),
                UtteranceRole.USER,
                "Second question",
                secondCorrelationId,
                UtteranceStatus.COMPLETED);

        publishLlmSuccess(firstCorrelationId, "Late stale answer");

        TaiUiState afterStaleFirstLlmSuccess = project();

        assertThat(afterStaleFirstLlmSuccess.conversationStatus())
                .isEqualTo(ConversationStatus.THINKING);
        assertModule(afterStaleFirstLlmSuccess, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertThat(afterStaleFirstLlmSuccess.lastAssistantUtterance()).isNull();
        assertUtterance(
                afterStaleFirstLlmSuccess.lastUserUtterance(),
                UtteranceRole.USER,
                "Second question",
                secondCorrelationId,
                UtteranceStatus.COMPLETED);

        publishLlmSuccess(secondCorrelationId, "Current answer");

        TaiUiState afterSecondLlmSuccess = project();

        assertThat(afterSecondLlmSuccess.conversationStatus())
                .isEqualTo(ConversationStatus.SPEAKING);
        assertModule(afterSecondLlmSuccess, TaiModule.LLM, ModuleHealth.UP, "Idle");
        assertModule(afterSecondLlmSuccess, TaiModule.TTS_PIPER, ModuleHealth.UP, "Synthesizing");
        assertUtterance(
                afterSecondLlmSuccess.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "Current answer",
                secondCorrelationId,
                UtteranceStatus.STARTED);
    }

    @Test
    void barge_in_while_tts_is_speaking_should_project_interrupted_assistant_and_new_user_turn() {
        String firstCorrelationId = "turn-speaking";
        String secondCorrelationId = "turn-barge-in";

        publishSttSpeechStarted(firstCorrelationId);
        project();

        publishSttAccepted(firstCorrelationId, "First question");
        project();

        publishLlmSuccess(firstCorrelationId, "First answer");
        project();

        publishTtsStarted(firstCorrelationId, "First answer");

        TaiUiState afterFirstTtsStarted = project();

        assertThat(afterFirstTtsStarted.conversationStatus())
                .isEqualTo(ConversationStatus.SPEAKING);
        assertModule(afterFirstTtsStarted, TaiModule.TTS_PIPER, ModuleHealth.UP, "Speaking");
        assertUtterance(
                afterFirstTtsStarted.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "First answer",
                firstCorrelationId,
                UtteranceStatus.STARTED);

        publishSttSpeechStarted(secondCorrelationId);

        TaiUiState afterBargeInSpeechStarted = project();

        assertThat(afterBargeInSpeechStarted.conversationStatus())
                .isEqualTo(ConversationStatus.LISTENING);
        assertModule(
                afterBargeInSpeechStarted, TaiModule.STT_LISTENER, ModuleHealth.UP, "Recording");
        assertModule(afterBargeInSpeechStarted, TaiModule.TTS_PIPER, ModuleHealth.UP, "Silent");
        assertUtterance(
                afterBargeInSpeechStarted.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "First answer",
                firstCorrelationId,
                UtteranceStatus.INTERRUPTED);

        publishSttAccepted(secondCorrelationId, "Interrupting question");

        TaiUiState afterSecondAccepted = project();

        assertThat(afterSecondAccepted.conversationStatus()).isEqualTo(ConversationStatus.THINKING);
        assertModule(afterSecondAccepted, TaiModule.LLM, ModuleHealth.UP, "Generating");
        assertUtterance(
                afterSecondAccepted.lastUserUtterance(),
                UtteranceRole.USER,
                "Interrupting question",
                secondCorrelationId,
                UtteranceStatus.COMPLETED);
        assertUtterance(
                afterSecondAccepted.lastAssistantUtterance(),
                UtteranceRole.ASSISTANT,
                "First answer",
                firstCorrelationId,
                UtteranceStatus.INTERRUPTED);
    }

    private TaiUiState project() {
        return taiUiStateProjectionService.rebuild();
    }

    private void assertModule(
            TaiUiState state, TaiModule module, ModuleHealth expectedHealth, String expectedState) {
        ModuleOverview overview = state.modules().get(module);

        assertThat(overview).as("%s overview", module).isNotNull();

        assertThat(overview.health()).as("%s health", module).isEqualTo(expectedHealth);

        assertThat(overview.state()).as("%s state", module).isEqualTo(expectedState);

        assertThat(overview.lastUpdateAt()).as("%s lastUpdateAt", module).isNotNull();
    }

    private void assertUtterance(
            Utterance utterance,
            UtteranceRole expectedRole,
            String expectedText,
            String expectedCorrelationId,
            UtteranceStatus expectedStatus) {
        assertThat(utterance).isNotNull();
        assertThat(utterance.role()).isEqualTo(expectedRole);
        assertThat(utterance.text()).isEqualTo(expectedText);
        assertThat(utterance.correlationId()).isEqualTo(expectedCorrelationId);
        assertThat(utterance.status()).isEqualTo(expectedStatus);
        assertThat(utterance.startedAt()).isNotNull();
        assertThat(utterance.updatedAt()).isNotNull();
    }
}
