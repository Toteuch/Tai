package com.toteuch.tai.taiorchestrator.state;

import org.springframework.stereotype.Component;

@Component
public class InMemoryStateStore implements StateStore {

    private final AssistantState state = new AssistantState();

    @Override
    public AssistantState get() {
        return state;
    }
}
