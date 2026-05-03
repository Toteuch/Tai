// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.ui;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiManualTextInputReceivedEventHandler
        implements EventHandler<UiManualTextInputReceivedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final TaiEventPublisher eventPublisher;

    public UiManualTextInputReceivedEventHandler(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.UI_MANUAL_TEXT_INPUT_RECEIVED;
    }

    @Override
    public void handle(UiManualTextInputReceivedEvent event) {
        perfLog.debug("Manual text input received | correlationId={}", event.correlationId());
        eventPublisher.publish(
                new UserUtteranceAcceptedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source(),
                        event.text(),
                        0L,
                        0L));
    }
}
