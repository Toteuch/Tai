package com.toteuch.tai.taiorchestrator.services.ui;

import com.toteuch.tai.taiorchestrator.state.AssistantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingUiClient implements UiClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingUiClient.class);

    @Override
    public void updateUserTranscript(String sessionId, String text) {
        log.info("UI user transcript | sessionId={} text={}", sessionId, text);
    }

    @Override
    public void updateAssistantReply(String sessionId, String text) {
        log.info("UI assistant reply | sessionId={} text={}", sessionId, text);
    }

    @Override
    public void updateAssistantState(String sessionId, AssistantState state) {
        log.info("UI state | sessionId={} listening={} thinking={} speaking={}",
            sessionId,
            state.getListeningState(),
            state.getThinkingState(),
            state.getSpeakingState());
    }

    @Override
    public void showError(String sessionId, String message) {
        log.error("UI error | sessionId={} message={}", sessionId, message);
    }
}
