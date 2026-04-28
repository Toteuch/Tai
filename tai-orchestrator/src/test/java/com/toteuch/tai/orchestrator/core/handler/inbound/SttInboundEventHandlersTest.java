// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.core.handler.inbound.stt.SttSpeechStartedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.stt.SttTranscriptAcceptedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.stt.SttTranscriptNoiseEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.stt.SttTranscriptUnintelligibleEventHandler;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttSpeechStartedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.events.internal.ClarificationRequestedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserSpeechStartedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SttInboundEventHandlersTest extends AbstractHandlerTest {
    private final SessionContext sessionContext = new SessionContext();
    private final SessionStore sessionStore = () -> sessionContext;

    @Test
    void accepted_transcript_should_publish_user_utterance_accepted_event() {
        SttTranscriptAcceptedEventHandler handler =
                new SttTranscriptAcceptedEventHandler(eventPublisher);

        handler.handle(
                new SttTranscriptAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.STT_SERVICE,
                        "Hello Tai",
                        "en",
                        0.98,
                        1400L,
                        600.0,
                        "ACCEPTED",
                        0,
                        1000L));

        UserUtteranceAcceptedEvent published =
                eventPublisher.assertSingleEventPublished(UserUtteranceAcceptedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.source()).isEqualTo(EventSource.STT_SERVICE);
        assertThat(published.text()).isEqualTo("Hello Tai");
    }

    @Test
    void unintelligible_transcript_should_publish_clarification_requested_event() {
        SttTranscriptUnintelligibleEventHandler handler =
                new SttTranscriptUnintelligibleEventHandler(eventPublisher);

        handler.handle(
                new SttTranscriptUnintelligibleEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.STT_SERVICE,
                        "fi",
                        0.42,
                        1200L,
                        500.0,
                        "UNSUPPORTED_LANGUAGE",
                        3,
                        1000L));

        ClarificationRequestedEvent published =
                eventPublisher.assertSingleEventPublished(ClarificationRequestedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-2");
        assertThat(published.source()).isEqualTo(EventSource.STT_SERVICE);
    }

    @Test
    void noise_transcript_should_publish_no_event() {
        SttTranscriptNoiseEventHandler handler = new SttTranscriptNoiseEventHandler();

        handler.handle(
                new SttTranscriptNoiseEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-3",
                        EventSource.STT_SERVICE,
                        500L,
                        70.0,
                        "NOISE",
                        999,
                        1000L));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void speech_started_should_publish_user_speech_started_event() {
        SttSpeechStartedEventHandler handler = new SttSpeechStartedEventHandler(eventPublisher);

        handler.handle(
                new SttSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-4",
                        EventSource.STT_SERVICE,
                        10L,
                        50.0));

        UserSpeechStartedEvent published =
                eventPublisher.assertSingleEventPublished(UserSpeechStartedEvent.class);
    }
}
