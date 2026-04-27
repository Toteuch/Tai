package com.toteuch.tai.orchestrator.core.handler.inbound.stt;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptNoiseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SttTranscriptNoiseEventHandler implements EventHandler<SttTranscriptNoiseEvent> {
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    @Override
    public EventType supports() {
        return EventType.STT_TRANSCRIPT_NOISE;
    }

    @Override
    public void handle(SttTranscriptNoiseEvent event) {
        perfLog.info(
                "STT noise received | correlationId={} transcriptionDurationMs={} durationMs={}",
                event.correlationId(),
                event.transcriptionDurationMs(),
                event.durationMs());
        decisionLog.info("STT noise ignored | correlationId={}", event.correlationId());
    }
}
