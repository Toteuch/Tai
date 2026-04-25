package com.toteuch.tai.taiorchestrator.core;

import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class EventDispatcher {

    private final List<EventHandler<? extends TaiEvent>> handlers;
    private final Map<EventType, EventHandler<? extends TaiEvent>> handlersByType = new EnumMap<>(EventType.class);

    public EventDispatcher(List<EventHandler<? extends TaiEvent>> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {
        for (EventHandler<? extends TaiEvent> handler : handlers) {
            handlersByType.put(handler.supports(), handler);
        }
    }

    @SuppressWarnings("unchecked")
    public void dispatch(TaiEvent event) {
        EventHandler<TaiEvent> handler = (EventHandler<TaiEvent>) handlersByType.get(event.type());
        if (handler == null) {
            throw new IllegalStateException("No handler registered for event type: " + event.type());
        }
        handler.handle(event);
    }
}
