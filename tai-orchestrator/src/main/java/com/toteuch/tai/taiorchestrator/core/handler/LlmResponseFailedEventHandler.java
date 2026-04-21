package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.LlmResponseFailedEvent;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import com.toteuch.tai.taiorchestrator.state.ThinkingState;
import org.springframework.stereotype.Component;

@Component
public class LlmResponseFailedEventHandler implements EventHandler<LlmResponseFailedEvent> {

    private final StateStore stateStore;
    private final UiClient uiClient;

    public LlmResponseFailedEventHandler(StateStore stateStore, UiClient uiClient) {
        this.stateStore = stateStore;
        this.uiClient = uiClient;
    }

    @Override
    public EventType supports() {
        return EventType.LLM_RESPONSE_FAILED;
    }

    @Override
    public void handle(LlmResponseFailedEvent event) {
        stateStore.get().setThinkingState(ThinkingState.IDLE);
        uiClient.showError(event.sessionId(), event.errorMessage());
        uiClient.updateAssistantState(event.sessionId(), stateStore.get());
    }
}
