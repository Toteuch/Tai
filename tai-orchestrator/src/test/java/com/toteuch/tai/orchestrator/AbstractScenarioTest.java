// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.events.llm.LlmResponseFailedEvent;
import com.toteuch.tai.events.stt.SttSpeechStartedEvent;
import com.toteuch.tai.events.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.events.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.events.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.events.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.events.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.events.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
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
public abstract class AbstractScenarioTest {

    public static String MODEL_NAME = "tai-model";
    @Autowired protected TaiEventPublisher eventPublisher;
    @Autowired protected SessionStore sessionStore;
    @MockitoBean protected LlmClient llmClient;
    @MockitoBean protected TtsClient ttsClient;

    public void publishLlmSuccess(String correlationId, String text) {
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

    public void publishLlmFailure(String correlationId) {
        eventPublisher.publish(
                new LlmResponseFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.LLM_SERVICE,
                        MODEL_NAME,
                        0L,
                        "LLM_ERROR",
                        "LLM failed"));
    }

    public void publishSttAccepted(String correlationId, String text) {
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
                        1000L));
    }

    public void publishSttUnintelligible(String correlationId) {
        eventPublisher.publish(
                new SttTranscriptUnintelligibleEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        "fi",
                        0.30,
                        600.0,
                        "UNSUPPORTED_LANGUAGE",
                        3,
                        1200L,
                        1000L));
    }

    public void publishSttNoise(String correlationId) {
        eventPublisher.publish(
                new SttTranscriptNoiseEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        80.0,
                        "NOISE",
                        999,
                        1200L,
                        1000L));
    }

    public void publishTtsStarted(String correlationId, String text) {
        eventPublisher.publish(
                new TtsPlaybackStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        text,
                        600L));
    }

    public void publishTtsCompleted(String correlationId, String text) {
        eventPublisher.publish(
                new TtsPlaybackCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        text,
                        1200L));
    }

    public void publishTtsFailed(String correlationId) {
        eventPublisher.publish(
                new TtsPlaybackFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        "PIPER_TTS_ERROR",
                        "Piper TTS failed",
                        0L));
    }

    public void publishSttSpeechStarted(String correlationId) {
        eventPublisher.publish(
                new SttSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        2.0,
                        10.0));
    }

    public void publishUserSpeechStarted(String correlationId) {
        eventPublisher.publish(
                new UserSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE));
    }
}
