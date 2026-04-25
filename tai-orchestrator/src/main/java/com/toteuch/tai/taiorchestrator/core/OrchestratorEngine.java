package com.toteuch.tai.taiorchestrator.core;

import com.toteuch.tai.taiorchestrator.events.TaiEvent;

public interface OrchestratorEngine {
    void handle(TaiEvent event);
}
