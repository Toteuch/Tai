package com.toteuch.tai.orchestrator.core.handler.inbound.tts;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechFailedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class TtsPlaybackFailedEventHandler implements EventHandler<TtsPlaybackFailedEvent> {
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public TtsPlaybackFailedEventHandler(
        SessionStore sessionStore,
        TaiEventPublisher eventPublisher
    ) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.TTS_PLAYBACK_FAILED;
    }

    @Override
    public void handle(TtsPlaybackFailedEvent event) {
        perfLog.info("TTS speech failed | correlationId={}", event.correlationId());
        SessionContext sessionContext = sessionStore.get();

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info("{} ignored | correlationId={} activeTurnCorrelationId={}",
                this.getClass().getSimpleName(),
                event.correlationId(),
                sessionContext.getActiveTurn().getCorrelationId());
            return;
        }

        eventPublisher.publish(new AssistantSpeechFailedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            event.correlationId(),
            EventSource.TTS_SERVICE,
            event.errorCode(),
            event.errorMessage()
        ));
    }
}
