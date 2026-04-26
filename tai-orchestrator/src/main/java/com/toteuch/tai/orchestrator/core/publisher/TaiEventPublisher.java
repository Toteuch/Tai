package com.toteuch.tai.orchestrator.core.publisher;

import com.toteuch.tai.orchestrator.events.TaiEvent;

public interface TaiEventPublisher {
    void publish(TaiEvent event);
}
