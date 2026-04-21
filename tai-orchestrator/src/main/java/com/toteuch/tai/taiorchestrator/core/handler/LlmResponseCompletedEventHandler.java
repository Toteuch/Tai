package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.state.AssistantState;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import com.toteuch.tai.taiorchestrator.state.ThinkingState;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LlmResponseCompletedEventHandler implements EventHandler<LlmResponseCompletedEvent> {

    private final StateStore stateStore;
    private final SessionStore sessionStore;
    private final UiClient uiClient;
    private final TtsClient ttsClient;

    public LlmResponseCompletedEventHandler(
        StateStore stateStore,
        SessionStore sessionStore,
        UiClient uiClient,
        TtsClient ttsClient
    ) {
        this.stateStore = stateStore;
        this.sessionStore = sessionStore;
        this.uiClient = uiClient;
        this.ttsClient = ttsClient;
    }

    @Override
    public EventType supports() {
        return EventType.LLM_RESPONSE_COMPLETED;
    }

    @Override
    public void handle(LlmResponseCompletedEvent event) {
        AssistantState state = stateStore.get();
        SessionContext sessionContext = sessionStore.getOrCreate(event.sessionId());

        state.setThinkingState(ThinkingState.IDLE);
        state.setCurrentAssistantText(event.responseText());

        String userText = state.getCurrentUserText();
        sessionContext.addTurn(new ConversationTurn(userText, event.responseText(), Instant.now()));
        sessionContext.setCurrentExecution(null);

        uiClient.updateAssistantReply(event.sessionId(), event.responseText());

        if (state.isTtsEnabled()) {
            state.setSpeakingState(SpeakingState.PREPARING);
            uiClient.updateAssistantState(event.sessionId(), state);
            ttsClient.speak(event.sessionId(), event.correlationId(), event.responseText());
        } else {
            state.setSpeakingState(SpeakingState.SILENT);
            uiClient.updateAssistantState(event.sessionId(), state);
        }
    }
}
