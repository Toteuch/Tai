package com.toteuch.tai.taiorchestrator.core.handler.inbound.ui;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.ui.UiTtsToggleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiTtsToggleChangedEventHandler implements EventHandler<UiTtsToggleChangedEvent> {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    @Override
    public EventType supports() {
        return EventType.UI_TTS_TOGGLE_CHANGED;
    }

    @Override
    public void handle(UiTtsToggleChangedEvent event) {
        perfLog.info("TTS toggle change received | correlationId={}", event.correlationId());
        errorLog.error("{} not supported yet", this.getClass().getSimpleName());
    }
}
