package com.toteuch.tai.orchestrator.core.handler.inbound.ui;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiObscenityFilterToggleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiObscenityFilterToggleChangedEventHandler
        implements EventHandler<UiObscenityFilterToggleChangedEvent> {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    @Override
    public EventType supports() {
        return EventType.UI_OBSCENITY_FILTER_TOGGLE_CHANGED;
    }

    @Override
    public void handle(UiObscenityFilterToggleChangedEvent event) {
        perfLog.debug(
                "Obscenity filter toggle change received | correlationId={}",
                event.correlationId());
        errorLog.error("{} not supported yet", this.getClass().getSimpleName());
    }
}
