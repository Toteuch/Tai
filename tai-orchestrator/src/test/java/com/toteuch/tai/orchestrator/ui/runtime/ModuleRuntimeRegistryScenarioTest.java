// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.AbstractScenarioTest;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ModuleRuntimeRegistryScenarioTest extends AbstractScenarioTest {

    @Autowired private ModuleRuntimeRegistry moduleRuntimeRegistry;

    @Test
    void nominal_voice_flow_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String correlationId = "turn-nominal";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(correlationId);

        RuntimeState afterSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, correlationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterSpeechStarted);

        publishSttAccepted(correlationId, "Hello Tai");

        RuntimeState afterSttAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, correlationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, correlationId),
                        initial.ttsPiper());
        assertRuntimeState(afterSttAccepted);

        publishLlmSuccess(correlationId, "Hello. How can I help?");

        RuntimeState afterLlmSuccess =
                new RuntimeState(
                        afterSttAccepted.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, correlationId),
                        expected(ModuleHealth.UP, ModuleActivity.SYNTHESIZING, correlationId));
        assertRuntimeState(afterLlmSuccess);

        publishTtsStarted(correlationId, "Hello. How can I help?");

        RuntimeState afterTtsStarted =
                new RuntimeState(
                        afterLlmSuccess.sttListener(),
                        initial.sttWhisper(),
                        afterLlmSuccess.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.SPEAKING, correlationId));
        assertRuntimeState(afterTtsStarted);

        publishTtsCompleted(correlationId, "Hello. How can I help?");

        RuntimeState afterTtsCompleted =
                new RuntimeState(
                        afterTtsStarted.sttListener(),
                        initial.sttWhisper(),
                        afterTtsStarted.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, correlationId));
        assertRuntimeState(afterTtsCompleted);
    }

    @Test
    void llm_failure_flow_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String correlationId = "turn-llm-failure";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(correlationId);

        RuntimeState afterSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, correlationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterSpeechStarted);

        publishSttAccepted(correlationId, "Tell me something");

        RuntimeState afterSttAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, correlationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, correlationId),
                        initial.ttsPiper());
        assertRuntimeState(afterSttAccepted);

        publishLlmFailure(correlationId);

        RuntimeState afterLlmFailure =
                new RuntimeState(
                        afterSttAccepted.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.DEGRADED, ModuleActivity.ERROR, correlationId),
                        afterSttAccepted.ttsPiper());
        assertRuntimeState(afterLlmFailure);
    }

    @Test
    void tts_failure_flow_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String correlationId = "turn-tts-failure";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(correlationId);

        RuntimeState afterSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, correlationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterSpeechStarted);

        publishSttAccepted(correlationId, "Say something");

        RuntimeState afterSttAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, correlationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, correlationId),
                        initial.ttsPiper());
        assertRuntimeState(afterSttAccepted);

        publishLlmSuccess(correlationId, "Something");

        RuntimeState afterLlmSuccess =
                new RuntimeState(
                        afterSttAccepted.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, correlationId),
                        expected(ModuleHealth.UP, ModuleActivity.SYNTHESIZING, correlationId));
        assertRuntimeState(afterLlmSuccess);

        publishTtsFailed(correlationId);

        RuntimeState afterTtsFailed =
                new RuntimeState(
                        afterLlmSuccess.sttListener(),
                        initial.sttWhisper(),
                        afterLlmSuccess.llm(),
                        expected(ModuleHealth.DEGRADED, ModuleActivity.ERROR, correlationId));
        assertRuntimeState(afterTtsFailed);
    }

    @Test
    void stt_noise_flow_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String correlationId = "turn-stt-noise";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(correlationId);

        RuntimeState afterSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, correlationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterSpeechStarted);

        publishSttNoise(correlationId);

        RuntimeState afterNoise =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, correlationId),
                        initial.sttWhisper(),
                        afterSpeechStarted.llm(),
                        afterSpeechStarted.ttsPiper());
        assertRuntimeState(afterNoise);
    }

    @Test
    void stt_unintelligible_flow_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String correlationId = "turn-stt-unintelligible";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(correlationId);

        RuntimeState afterSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, correlationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterSpeechStarted);

        publishSttUnintelligible(correlationId);

        RuntimeState afterUnintelligible =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, correlationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, correlationId),
                        afterSpeechStarted.ttsPiper());
        assertRuntimeState(afterUnintelligible);

        publishLlmSuccess(correlationId, "Could you repeat that?");

        RuntimeState afterClarificationLlmSuccess =
                new RuntimeState(
                        afterUnintelligible.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, correlationId),
                        expected(ModuleHealth.UP, ModuleActivity.SYNTHESIZING, correlationId));
        assertRuntimeState(afterClarificationLlmSuccess);

        publishTtsStarted(correlationId, "Could you repeat that?");

        RuntimeState afterClarificationTtsStarted =
                new RuntimeState(
                        afterClarificationLlmSuccess.sttListener(),
                        initial.sttWhisper(),
                        afterClarificationLlmSuccess.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.SPEAKING, correlationId));
        assertRuntimeState(afterClarificationTtsStarted);

        publishTtsCompleted(correlationId, "Could you repeat that?");

        RuntimeState afterClarificationTtsCompleted =
                new RuntimeState(
                        afterClarificationTtsStarted.sttListener(),
                        initial.sttWhisper(),
                        afterClarificationTtsStarted.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, correlationId));
        assertRuntimeState(afterClarificationTtsCompleted);
    }

    @Test
    void
            barge_in_while_llm_is_generating_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String firstCorrelationId = "turn-first";
        String secondCorrelationId = "turn-second";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(firstCorrelationId);

        RuntimeState afterFirstSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, firstCorrelationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterFirstSpeechStarted);

        publishSttAccepted(firstCorrelationId, "First question");

        RuntimeState afterFirstAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, firstCorrelationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, firstCorrelationId),
                        initial.ttsPiper());
        assertRuntimeState(afterFirstAccepted);

        publishSttSpeechStarted(secondCorrelationId);

        RuntimeState afterSecondSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, secondCorrelationId),
                        initial.sttWhisper(),
                        afterFirstAccepted.llm(),
                        afterFirstAccepted.ttsPiper());
        assertRuntimeState(afterSecondSpeechStarted);

        publishSttAccepted(secondCorrelationId, "Second question");

        RuntimeState afterSecondAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, secondCorrelationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, secondCorrelationId),
                        afterSecondSpeechStarted.ttsPiper());
        assertRuntimeState(afterSecondAccepted);

        publishLlmSuccess(firstCorrelationId, "Late stale answer");

        RuntimeState afterStaleFirstLlmSuccess = afterSecondAccepted;
        assertRuntimeState(afterStaleFirstLlmSuccess);

        publishLlmSuccess(secondCorrelationId, "Current answer");

        RuntimeState afterSecondLlmSuccess =
                new RuntimeState(
                        afterSecondAccepted.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, secondCorrelationId),
                        expected(
                                ModuleHealth.UP, ModuleActivity.SYNTHESIZING, secondCorrelationId));
        assertRuntimeState(afterSecondLlmSuccess);
    }

    @Test
    void
            barge_in_while_tts_is_synthesizing_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String firstCorrelationId = "turn-synthesizing";
        String secondCorrelationId = "turn-barge-in";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(firstCorrelationId);

        RuntimeState afterFirstSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, firstCorrelationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterFirstSpeechStarted);

        publishSttAccepted(firstCorrelationId, "First question");

        RuntimeState afterFirstAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, firstCorrelationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, firstCorrelationId),
                        initial.ttsPiper());
        assertRuntimeState(afterFirstAccepted);

        publishLlmSuccess(firstCorrelationId, "First answer");

        RuntimeState afterFirstLlmSuccess =
                new RuntimeState(
                        afterFirstAccepted.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, firstCorrelationId),
                        expected(ModuleHealth.UP, ModuleActivity.SYNTHESIZING, firstCorrelationId));
        assertRuntimeState(afterFirstLlmSuccess);

        publishSttSpeechStarted(secondCorrelationId);

        RuntimeState afterSecondSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, secondCorrelationId),
                        initial.sttWhisper(),
                        afterFirstLlmSuccess.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, firstCorrelationId));
        assertRuntimeState(afterSecondSpeechStarted);

        publishSttAccepted(secondCorrelationId, "Interrupting question");

        RuntimeState afterSecondAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, secondCorrelationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, secondCorrelationId),
                        afterSecondSpeechStarted.ttsPiper());
        assertRuntimeState(afterSecondAccepted);
    }

    @Test
    void
            barge_in_while_tts_is_speaking_should_expose_expected_runtime_modules_after_each_inbound_event() {
        String firstCorrelationId = "turn-speaking";
        String secondCorrelationId = "turn-barge-in";

        RuntimeState initial = initialState();

        publishSttSpeechStarted(firstCorrelationId);

        RuntimeState afterFirstSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, firstCorrelationId),
                        initial.sttWhisper(),
                        initial.llm(),
                        initial.ttsPiper());
        assertRuntimeState(afterFirstSpeechStarted);

        publishSttAccepted(firstCorrelationId, "First question");

        RuntimeState afterFirstAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, firstCorrelationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, firstCorrelationId),
                        initial.ttsPiper());
        assertRuntimeState(afterFirstAccepted);

        publishLlmSuccess(firstCorrelationId, "First answer");

        RuntimeState afterFirstLlmSuccess =
                new RuntimeState(
                        afterFirstAccepted.sttListener(),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, firstCorrelationId),
                        expected(ModuleHealth.UP, ModuleActivity.SYNTHESIZING, firstCorrelationId));
        assertRuntimeState(afterFirstLlmSuccess);

        publishTtsStarted(firstCorrelationId, "First answer");

        RuntimeState afterFirstTtsStarted =
                new RuntimeState(
                        afterFirstLlmSuccess.sttListener(),
                        initial.sttWhisper(),
                        afterFirstLlmSuccess.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.SPEAKING, firstCorrelationId));
        assertRuntimeState(afterFirstTtsStarted);

        publishSttSpeechStarted(secondCorrelationId);

        RuntimeState afterSecondSpeechStarted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.CAPTURING, secondCorrelationId),
                        initial.sttWhisper(),
                        afterFirstTtsStarted.llm(),
                        expected(ModuleHealth.UP, ModuleActivity.IDLE, firstCorrelationId));
        assertRuntimeState(afterSecondSpeechStarted);

        publishSttAccepted(secondCorrelationId, "Interrupting question");

        RuntimeState afterSecondAccepted =
                new RuntimeState(
                        expected(ModuleHealth.UP, ModuleActivity.LISTENING, secondCorrelationId),
                        initial.sttWhisper(),
                        expected(ModuleHealth.UP, ModuleActivity.GENERATING, secondCorrelationId),
                        afterSecondSpeechStarted.ttsPiper());
        assertRuntimeState(afterSecondAccepted);
    }

    private RuntimeState initialState() {
        return new RuntimeState(
                expected(ModuleHealth.DEGRADED, ModuleActivity.UNKNOWN, null),
                expected(ModuleHealth.DEGRADED, ModuleActivity.UNKNOWN, null),
                expected(ModuleHealth.DEGRADED, ModuleActivity.UNKNOWN, null),
                expected(ModuleHealth.DEGRADED, ModuleActivity.UNKNOWN, null));
    }

    private RuntimeExpectation expected(
            ModuleHealth health, ModuleActivity activity, String correlationId) {
        return new RuntimeExpectation(health, activity, correlationId);
    }

    private void assertRuntimeState(RuntimeState expectedState) {
        assertRuntime(TaiModule.STT_LISTENER, expectedState.sttListener());
        assertRuntime(TaiModule.STT_WHISPER, expectedState.sttWhisper());
        assertRuntime(TaiModule.LLM, expectedState.llm());
        assertRuntime(TaiModule.TTS_PIPER, expectedState.ttsPiper());
    }

    private void assertRuntime(TaiModule module, RuntimeExpectation expected) {
        ModuleRuntimeSnapshot snapshot = moduleRuntimeRegistry.get(module);

        assertThat(snapshot).as("%s runtime snapshot", module).isNotNull();

        assertThat(snapshot.health()).as("%s health", module).isEqualTo(expected.health());

        assertThat(snapshot.lastActivity())
                .as("%s activity", module)
                .isEqualTo(expected.activity());

        assertThat(snapshot.lastActiveCorrelationId())
                .as("%s lastActiveCorrelationId", module)
                .isEqualTo(expected.correlationId());

        assertThat(snapshot.lastActivityAt()).as("%s lastActivityAt", module).isNotNull();
    }

    private record RuntimeState(
            RuntimeExpectation sttListener,
            RuntimeExpectation sttWhisper,
            RuntimeExpectation llm,
            RuntimeExpectation ttsPiper) {}

    private record RuntimeExpectation(
            ModuleHealth health, ModuleActivity activity, String correlationId) {}
}
