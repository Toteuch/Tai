package com.toteuch.tai.taiorchestrator.core;

import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;

public interface EventHandler<T extends TaiEvent> {
    EventType supports();
    void handle(T event);
}
