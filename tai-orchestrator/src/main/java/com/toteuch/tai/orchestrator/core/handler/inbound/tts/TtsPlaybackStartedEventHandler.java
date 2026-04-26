package com.toteuch.tai.orchestrator.core.handler.inbound.tts;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TtsPlaybackStartedEventHandler implements EventHandler<TtsPlaybackStartedEvent> {
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public TtsPlaybackStartedEventHandler(
            SessionStore sessionStore, TaiEventPublisher eventPublisher) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.TTS_PLAYBACK_STARTED;
    }

    @Override
    public void handle(TtsPlaybackStartedEvent event) {
        perfLog.info("TTS speech started | correlationId={}", event.correlationId());
        SessionContext sessionContext = sessionStore.get();

        if (sessionContext.getActiveTurn() == null) {
            // TODO : handle that case to send TtsClient.stop()
            decisionLog.info(
                    "{} ignored : no active turn | correlationId={}",
                    this.getClass().getSimpleName(),
                    event.correlationId());
            return;
        }

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info(
                    "{} ignored: stalled correlationId | correlationId={} activeTurnCorrelationId={}",
                    this.getClass().getSimpleName(),
                    event.correlationId(),
                    sessionContext.getActiveTurn().getCorrelationId());
            return;
        }

        eventPublisher.publish(
                new AssistantSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        event.correlationId(),
                        EventSource.TTS_SERVICE));
    }
}
