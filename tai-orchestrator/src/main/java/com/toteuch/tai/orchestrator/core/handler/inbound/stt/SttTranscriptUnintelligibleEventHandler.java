package com.toteuch.tai.orchestrator.core.handler.inbound.stt;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.events.internal.ClarificationRequestedEvent;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SttTranscriptUnintelligibleEventHandler
        implements EventHandler<SttTranscriptUnintelligibleEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final TaiEventPublisher eventPublisher;

    public SttTranscriptUnintelligibleEventHandler(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.STT_TRANSCRIPT_UNINTELLIGIBLE;
    }

    @Override
    public void handle(SttTranscriptUnintelligibleEvent event) {
        perfLog.info(
                "STT unintelligible speech received | correlationId={} transcriptionDurationMs={}",
                event.correlationId(),
                event.transcriptionDurationMs());
        eventPublisher.publish(
                new ClarificationRequestedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        event.correlationId(),
                        event.source()));
    }
}
