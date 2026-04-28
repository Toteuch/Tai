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
import com.toteuch.tai.orchestrator.session.TurnMetrics;
import com.toteuch.tai.orchestrator.session.TurnMetricsOutcome;
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

        TurnMetrics turnMetrics = sessionContext.getTurnMetrics(newCorrelationId);
        turnMetrics.setUserSpeechStartAt(event.occurredAt());

        if (activeTurn != null && !sessionContext.isStillActiveTurn(newCorrelationId)) {
            TurnMetricsOutcome outcome = null;
            if (sessionContext.isTtsEnabled()
                    && (sessionContext.getSpeakingState() == SpeakingState.SPEAKING
                            || sessionContext.getSpeakingState() == SpeakingState.PREPARING)) {
                contextLog.info(
                        "Barge-in detected during assistant speech | correlationId={} interruptedCorrelationId={}",
                        newCorrelationId,
                        activeTurn.getCorrelationId());
                activeTurn.setAssistantPlaybackInterrupted(true);
                sessionContext.setSpeakingState(SpeakingState.SILENT);
                outcome = TurnMetricsOutcome.INTERRUPTED;
                perfLog.debug(
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
                outcome = TurnMetricsOutcome.SUPERSEDED;
            }
            if (outcome == null) {
                outcome = TurnMetricsOutcome.UNKNOWN;
            }
            sessionContext.logMetrics(activeTurn.getCorrelationId(), outcome);
            sessionContext.addTurn(sessionContext.getActiveTurn());
            sessionContext.setActiveTurn(null);
        }
    }
}
