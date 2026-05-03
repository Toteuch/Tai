// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.tts;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
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
        perfLog.debug(
                "TTS speech started | correlationId={} synthesisDurationMs={}",
                event.correlationId(),
                event.synthesisDurationMs());
        SessionContext sessionContext = sessionStore.get();

        if (sessionContext.getActiveTurn() == null) {
            decisionLog.info(
                    "{} ignored : no active turn | correlationId={}",
                    event.getClass().getSimpleName(),
                    event.correlationId());
            return;
        }

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info(
                    "{} ignored: stalled correlationId | correlationId={} activeTurnCorrelationId={}",
                    event.getClass().getSimpleName(),
                    event.correlationId(),
                    sessionContext.getActiveTurn().getCorrelationId());
            return;
        }

        eventPublisher.publish(
                new AssistantSpeechStartedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source(),
                        event.text(),
                        event.synthesisDurationMs()));
    }
}
