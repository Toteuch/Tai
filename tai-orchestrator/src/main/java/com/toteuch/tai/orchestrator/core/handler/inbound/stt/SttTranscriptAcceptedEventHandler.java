package com.toteuch.tai.orchestrator.core.handler.inbound.stt;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SttTranscriptAcceptedEventHandler implements EventHandler<SttTranscriptAcceptedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final TaiEventPublisher eventPublisher;

    public SttTranscriptAcceptedEventHandler(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.STT_TRANSCRIPT_ACCEPTED;
    }

    @Override
    public void handle(SttTranscriptAcceptedEvent event) {
        perfLog.info(
                "STT utterance completed received | correlationId={} durationMs={}",
                event.correlationId(),
                event.durationMs());
        eventPublisher.publish(
                new UserUtteranceAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        event.correlationId(),
                        EventSource.ORCHESTRATOR,
                        event.text()));
    }
}
