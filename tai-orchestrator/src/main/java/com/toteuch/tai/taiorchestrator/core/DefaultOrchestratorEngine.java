package com.toteuch.tai.taiorchestrator.core;

import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrchestratorEngine implements OrchestratorEngine {

    private final EventDispatcher eventDispatcher;

    public DefaultOrchestratorEngine(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void handle(TaiEvent event) {
        eventDispatcher.dispatch(event);
    }
}
