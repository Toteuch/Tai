// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.internal.AssistantStopSpeakReceivedEvent;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import com.toteuch.tai.orchestrator.session.TurnOutcome;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshReason;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshRequester;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AssistantStopSpeakReceivedEventHandler
        implements EventHandler<AssistantStopSpeakReceivedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");

    private final SessionStore sessionStore;
    private final TtsClient ttsClient;
    private final ModuleRuntimeUpdater runtimeUpdater;
    private final UiStateRefreshRequester uiStateRefreshRequester;

    public AssistantStopSpeakReceivedEventHandler(
            SessionStore sessionStore,
            TtsClient ttsClient,
            ModuleRuntimeUpdater runtimeUpdater,
            UiStateRefreshRequester uiStateRefreshRequester) {
        this.sessionStore = sessionStore;
        this.ttsClient = ttsClient;
        this.runtimeUpdater = runtimeUpdater;
        this.uiStateRefreshRequester = uiStateRefreshRequester;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_STOP_SPEAK_RECEIVED;
    }

    @Override
    public void handle(AssistantStopSpeakReceivedEvent event) {
        SessionContext sessionContext = sessionStore.get();
        if (sessionContext.isStillActiveTurn(event.correlationId())) {
            TurnOutcome outcome = null;
            ConversationTurn activeTurn = sessionContext.getActiveTurn();
            if (sessionContext.getSpeakingState() == SpeakingState.SPEAKING
                    || sessionContext.getSpeakingState() == SpeakingState.PREPARING) {
                contextLog.info(
                        "Assistant stop speak received during assistant speech | correlationId={}",
                        event.correlationId());
                activeTurn.setAssistantPlaybackInterrupted(true);
                sessionContext.setSpeakingState(SpeakingState.SILENT);
                outcome = TurnOutcome.INTERRUPTED;
                perfLog.debug("TTS stop speech called | correlationId={}", event.correlationId());
                ttsClient.stop(event.correlationId());
                runtimeUpdater.ttsIdle();
            }
            if (sessionContext.getThinkingState() == ThinkingState.GENERATING) {
                contextLog.info(
                        "Assistant stop speak received during assistant thinking | correlationId={}",
                        event.correlationId());
                sessionContext.setThinkingState(ThinkingState.IDLE);
                runtimeUpdater.llmIdle();
                outcome = TurnOutcome.INTERRUPTED;
            }
            if (outcome == null) {
                outcome = TurnOutcome.UNKNOWN;
            }
            sessionContext.logMetrics(activeTurn.getCorrelationId(), outcome);
            sessionContext.addTurn(sessionContext.getActiveTurn(), outcome);
            sessionContext.setActiveTurn(null);
        }
        uiStateRefreshRequester.requestRefresh(
                UiStateRefreshReason.RUNTIME_EVENT, event.correlationId());
    }
}
