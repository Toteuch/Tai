package com.toteuch.tai.orchestrator.core.handler.inbound.stt;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttSpeechStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SttSpeechStartedEventHandler implements EventHandler<SttSpeechStartedEvent> {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    @Override
    public EventType supports() {
        return EventType.STT_SPEECH_STARTED;
    }

    @Override
    public void handle(SttSpeechStartedEvent event) {
        perfLog.info("STT speech started call received | correlationId={}", event.correlationId());
        errorLog.error("{} not supported yet", this.getClass().getSimpleName());
    }
}
