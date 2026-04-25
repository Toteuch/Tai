package com.toteuch.tai.taiorchestrator.core.publisher;

import com.toteuch.tai.taiorchestrator.events.TaiEvent;

public interface TaiEventPublisher {
    void publish(TaiEvent event);
}
