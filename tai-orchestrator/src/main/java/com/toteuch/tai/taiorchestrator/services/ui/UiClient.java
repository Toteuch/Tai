package com.toteuch.tai.taiorchestrator.services.ui;

import com.toteuch.tai.taiorchestrator.state.AssistantState;

public interface UiClient {
    void updateUserTranscript(String sessionId, String text);

    void updateAssistantReply(String sessionId, String text);

    void updateAssistantState(String sessionId, AssistantState state);

    void showError(String sessionId, String message);
}
