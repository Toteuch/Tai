package com.toteuch.tai.taiorchestrator.transport;

import com.toteuch.tai.taiorchestrator.core.OrchestratorEngine;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaiSpringEventBridge {

    private final OrchestratorEngine orchestratorEngine;

    public TaiSpringEventBridge(OrchestratorEngine orchestratorEngine) {
        this.orchestratorEngine = orchestratorEngine;
    }

    @EventListener
    public void onTaiEvent(TaiEvent event) {
        orchestratorEngine.handle(event);
    }
}
