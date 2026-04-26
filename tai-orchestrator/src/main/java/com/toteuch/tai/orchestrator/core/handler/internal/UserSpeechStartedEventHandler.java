package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.internal.UserSpeechStartedEvent;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserSpeechStartedEventHandler implements EventHandler<UserSpeechStartedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");

    private final SessionStore sessionStore;
    private final TtsClient ttsClient;

    public UserSpeechStartedEventHandler(SessionStore sessionStore, TtsClient ttsClient) {
        this.sessionStore = sessionStore;
        this.ttsClient = ttsClient;
    }

    @Override
    public EventType supports() {
        return EventType.USER_SPEECH_STARTED;
    }

    @Override
    public void handle(UserSpeechStartedEvent event) {
        SessionContext sessionContext = sessionStore.get();

        ConversationTurn activeTurn = sessionStore.get().getActiveTurn();
        String newCorrelationId = event.correlationId();

        if (activeTurn != null && !sessionContext.isStillActiveTurn(newCorrelationId)) {
            if (sessionContext.isTtsEnabled()
                    && (sessionContext.getSpeakingState() == SpeakingState.SPEAKING
                            || sessionContext.getSpeakingState() == SpeakingState.PREPARING)) {
                contextLog.info(
                        "Barge-in detected during assistant speech | correlationId={} interruptedCorrelationId={}",
                        newCorrelationId,
                        activeTurn.getCorrelationId());
                activeTurn.setAssistantPlaybackInterrupted(true);
                sessionContext.setSpeakingState(SpeakingState.SILENT);
                perfLog.info(
                        "TTS stop speech called | correlationId={} activeTurnCorrelationId={}",
                        event.correlationId(),
                        activeTurn.getCorrelationId());
                ttsClient.stop(activeTurn.getCorrelationId());
            } else if (sessionContext.getThinkingState() == ThinkingState.GENERATING) {
                contextLog.info(
                        "Barge-in detected during assistant thinking | correlationId={} interruptedCorrelationId={}",
                        newCorrelationId,
                        activeTurn.getCorrelationId());
                sessionContext.setThinkingState(ThinkingState.IDLE);
                activeTurn.setSupersededBeforeAssistantReply(true);
                activeTurn.setSupersededByCorrelationId(newCorrelationId);
            }
            sessionContext.addTurn(sessionContext.getActiveTurn());
            sessionContext.setActiveTurn(null);
        }
    }
}
