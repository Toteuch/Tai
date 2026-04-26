package com.toteuch.tai.orchestrator.core;

import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;

public interface EventHandler<T extends TaiEvent> {
    EventType supports();

    void handle(T event);
}
