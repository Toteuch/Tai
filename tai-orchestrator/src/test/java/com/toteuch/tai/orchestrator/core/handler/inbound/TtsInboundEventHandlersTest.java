package com.toteuch.tai.orchestrator.core.handler.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.core.handler.inbound.tts.TtsPlaybackCompletedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.tts.TtsPlaybackFailedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.tts.TtsPlaybackStartedEventHandler;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechCompletedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TtsInboundEventHandlersTest extends AbstractHandlerTest {

    private final SessionContext sessionContext = new SessionContext();

    private final SessionStore sessionStore = () -> sessionContext;

    @Test
    void playback_started_for_active_turn_should_publish_assistant_speech_started_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true));

        TtsPlaybackStartedEventHandler handler =
                new TtsPlaybackStartedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new TtsPlaybackStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.TTS_SERVICE,
                        "Hi!",
                        "alba",
                        600L));

        AssistantSpeechStartedEvent published =
                eventPublisher.assertSingleEventPublished(AssistantSpeechStartedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.source()).isEqualTo(EventSource.TTS_SERVICE);
    }

    @Test
    void playback_started_for_stale_turn_should_publish_no_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));

        TtsPlaybackStartedEventHandler handler =
                new TtsPlaybackStartedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new TtsPlaybackStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "stale-corr",
                        EventSource.TTS_SERVICE,
                        "Late speech",
                        "alba",
                        600L));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void playback_completed_for_active_turn_should_publish_assistant_speech_completed_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("corr-2", "Hello Tai", Instant.now(), true));

        TtsPlaybackCompletedEventHandler handler =
                new TtsPlaybackCompletedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new TtsPlaybackCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.TTS_SERVICE,
                        "Hi!",
                        1200L));

        AssistantSpeechCompletedEvent published =
                eventPublisher.assertSingleEventPublished(AssistantSpeechCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-2");
        assertThat(published.source()).isEqualTo(EventSource.TTS_SERVICE);
    }

    @Test
    void playback_completed_for_stale_turn_should_publish_no_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));

        TtsPlaybackCompletedEventHandler handler =
                new TtsPlaybackCompletedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new TtsPlaybackCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "stale-corr",
                        EventSource.TTS_SERVICE,
                        "Late speech",
                        1200L));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void playback_failed_for_active_turn_should_publish_assistant_speech_failed_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("corr-3", "Hello Tai", Instant.now(), true));

        TtsPlaybackFailedEventHandler handler =
                new TtsPlaybackFailedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new TtsPlaybackFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-3",
                        EventSource.TTS_SERVICE,
                        "TTS_ERROR",
                        "TTS failed"));

        AssistantSpeechFailedEvent published =
                eventPublisher.assertSingleEventPublished(AssistantSpeechFailedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-3");
        assertThat(published.source()).isEqualTo(EventSource.TTS_SERVICE);
        assertThat(published.errorCode()).isEqualTo("TTS_ERROR");
        assertThat(published.errorMessage()).isEqualTo("TTS failed");
    }

    @Test
    void playback_failed_for_stale_turn_should_publish_no_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));

        TtsPlaybackFailedEventHandler handler =
                new TtsPlaybackFailedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new TtsPlaybackFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "stale-corr",
                        EventSource.TTS_SERVICE,
                        "TTS_ERROR",
                        "TTS failed"));

        eventPublisher.assertNoEventPublished();
    }
}
