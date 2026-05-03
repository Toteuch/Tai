// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.stt;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
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
        perfLog.debug(
                "STT utterance completed received | correlationId={} userSpeechDurationMs={} transcriptionDurationMs={}",
                event.correlationId(),
                event.userSpeechDurationMs(),
                event.transcriptionDurationMs());
        eventPublisher.publish(
                new UserUtteranceAcceptedEvent(
                        event.eventId(),
                        event.occurredAt(),
                        event.correlationId(),
                        event.source(),
                        event.transcript(),
                        event.userSpeechDurationMs(),
                        event.transcriptionDurationMs()));
    }
}
