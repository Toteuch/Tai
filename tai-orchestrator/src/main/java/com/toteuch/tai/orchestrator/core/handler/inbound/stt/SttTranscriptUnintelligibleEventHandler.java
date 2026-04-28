// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.inbound.stt;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.events.internal.ClarificationRequestedEvent;
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
        perfLog.debug(
                "STT unintelligible speech received | correlationId={} transcriptionDurationMs={}",
                event.correlationId(),
                event.transcriptionDurationMs());
        eventPublisher.publish(
                new ClarificationRequestedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source(),
                        event.transcriptionDurationMs()));
    }
}
