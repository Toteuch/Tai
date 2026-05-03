// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.ui;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.ui.UiStopSpeakReceivedEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.AssistantStopSpeakReceivedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiStopSpeakReceivedEventHandler implements EventHandler<UiStopSpeakReceivedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public UiStopSpeakReceivedEventHandler(
            SessionStore sessionStore, TaiEventPublisher eventPublisher) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.UI_STOP_SPEAK_RECEIVED;
    }

    @Override
    public void handle(UiStopSpeakReceivedEvent event) {
        perfLog.debug("Stop speak request received | correlationId={}", event.correlationId());

        SessionContext sessionContext = sessionStore.get();

        if (!sessionContext.isStillActiveTurn(event.correlationId())) {
            decisionLog.info(
                    "{} ignored | correlationId={} activeTurnCorrelationId={}",
                    event.getClass().getSimpleName(),
                    event.correlationId(),
                    sessionContext.getActiveTurn() != null
                            ? sessionContext.getActiveTurn().getCorrelationId()
                            : null);
            return;
        }

        eventPublisher.publish(
                new AssistantStopSpeakReceivedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source()));
    }
}
