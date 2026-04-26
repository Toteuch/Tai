package com.toteuch.tai.orchestrator.core.handler.inbound.stt;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttSpeechStartedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserSpeechStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SttSpeechStartedEventHandler implements EventHandler<SttSpeechStartedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final TaiEventPublisher eventPublisher;

    public SttSpeechStartedEventHandler(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.STT_SPEECH_STARTED;
    }

    @Override
    public void handle(SttSpeechStartedEvent event) {
        perfLog.info("STT speech started call received | correlationId={}", event.correlationId());
        eventPublisher.publish(new UserSpeechStartedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            event.correlationId(),
            EventSource.ORCHESTRATOR
        ));
    }
}
