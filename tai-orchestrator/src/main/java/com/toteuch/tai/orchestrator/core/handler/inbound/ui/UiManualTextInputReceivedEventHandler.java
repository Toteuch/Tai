package com.toteuch.tai.orchestrator.core.handler.inbound.ui;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import java.time.Instant;
import java.util.UUID;
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
        perfLog.info("Manual text input received | correlationId={}", event.correlationId());
        eventPublisher.publish(
                new UserUtteranceAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        event.correlationId(),
                        EventSource.UI,
                        event.text()));
    }
}
