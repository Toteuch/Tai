package com.toteuch.tai.taiorchestrator.core.publisher;

import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultTaiEventPublisher implements TaiEventPublisher {

    private final ObjectProvider<EventDispatcher> eventDispatcherProvider;

    public DefaultTaiEventPublisher(ObjectProvider<EventDispatcher> eventDispatcherProvider) {
        this.eventDispatcherProvider = eventDispatcherProvider;
    }

    @Override
    public void publish(TaiEvent event) {
        eventDispatcherProvider.getObject().dispatch(event);
    }
}
