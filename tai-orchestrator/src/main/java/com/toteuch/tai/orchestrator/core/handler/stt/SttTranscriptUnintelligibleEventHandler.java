// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.stt;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
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
                "STT unintelligible speech received | correlationId={} userSpeechDurationMs={} transcriptionDurationMs={}",
                event.correlationId(),
                event.speechDurationMs(),
                event.transcriptionDurationMs());
        eventPublisher.publish(
                new ClarificationRequestedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source(),
                        event.speechDurationMs(),
                        event.transcriptionDurationMs()));
    }
}
