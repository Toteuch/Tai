package com.toteuch.tai.orchestrator.core.scenario;

import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttSpeechStartedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserSpeechStartedEvent;
import com.toteuch.tai.orchestrator.services.llm.LlmClient;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
abstract class AbstractScenarioTest {

    protected static String MODEL_NAME = "tai-model";
    @Autowired protected TaiEventPublisher eventPublisher;
    @Autowired protected SessionStore sessionStore;
    @MockitoBean protected LlmClient llmClient;
    @MockitoBean protected TtsClient ttsClient;

    protected void publishLlmSuccess(String correlationId, String text) {
        eventPublisher.publish(
                new LlmResponseCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.LLM_SERVICE,
                        text,
                        MODEL_NAME,
                        10,
                        20,
                        100L));
    }

    protected void publishLlmFailure(String correlationId) {
        eventPublisher.publish(
                new LlmResponseFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.LLM_SERVICE,
                        MODEL_NAME,
                        "LLM_ERROR",
                        "LLM failed"));
    }

    protected void publishSttAccepted(String correlationId, String text) {
        eventPublisher.publish(
                new SttTranscriptAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        text,
                        "en",
                        0.98,
                        1500L,
                        700.0,
                        "ACCEPTED",
                        0,
                        1000L));
    }

    protected void publishSttUnintelligible(String correlationId) {
        eventPublisher.publish(
                new SttTranscriptUnintelligibleEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        "fi",
                        0.30,
                        1200L,
                        600.0,
                        "UNSUPPORTED_LANGUAGE",
                        3,
                        1000L));
    }

    protected void publishSttNoise(String correlationId) {
        eventPublisher.publish(
                new SttTranscriptNoiseEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        400L,
                        80.0,
                        "NOISE",
                        999,
                        1000L));
    }

    protected void publishTtsStarted(String correlationId, String text) {
        eventPublisher.publish(
                new TtsPlaybackStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        text,
                        "en_GB-alba-medium",
                        600L));
    }

    protected void publishTtsCompleted(String correlationId, String text) {
        eventPublisher.publish(
                new TtsPlaybackCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        text,
                        1200L));
    }

    protected void publishTtsFailed(String correlationId) {
        eventPublisher.publish(
                new TtsPlaybackFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        "PIPER_TTS_ERROR",
                        "Piper TTS failed"));
    }

    protected void publishSttSpeechStarted(String correlationId) {
        eventPublisher.publish(
                new SttSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        50L,
                        10.0));
    }

    protected void publishUserSpeechStarted(String correlationId) {
        eventPublisher.publish(
                new UserSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE));
    }
}
