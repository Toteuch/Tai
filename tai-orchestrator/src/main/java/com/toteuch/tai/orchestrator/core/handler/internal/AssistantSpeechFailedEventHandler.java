// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.TurnOutcome;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshReason;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshRequester;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AssistantSpeechFailedEventHandler implements EventHandler<AssistantSpeechFailedEvent> {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;
    private final ModuleRuntimeUpdater runtimeUpdater;
    private final UiStateRefreshRequester uiStateRefreshRequester;

    public AssistantSpeechFailedEventHandler(
            SessionStore sessionStore,
            TaiEventPublisher eventPublisher,
            ModuleRuntimeUpdater runtimeUpdater,
            UiStateRefreshRequester uiStateRefreshRequester) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
        this.runtimeUpdater = runtimeUpdater;
        this.uiStateRefreshRequester = uiStateRefreshRequester;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_SPEECH_FAILED;
    }

    @Override
    public void handle(AssistantSpeechFailedEvent event) {
        SessionContext sessionContext = sessionStore.get();
        sessionContext
                .getTurnMetrics(event.correlationId())
                .setTtsSpeechDurationMs(event.ttsSpeechDurationMs());

        errorLog.error(
                "TTS speech failed | correlationId={} errorCode={} errorMessage={}",
                event.correlationId(),
                event.errorCode(),
                event.errorMessage());

        sessionContext.setSpeakingState(SpeakingState.SILENT);
        runtimeUpdater.ttsError();

        eventPublisher.publish(
                new ConversationTurnCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        event.correlationId(),
                        EventSource.ORCHESTRATOR,
                        TurnOutcome.FAILED));

        uiStateRefreshRequester.requestRefresh(
                UiStateRefreshReason.RUNTIME_EVENT, event.correlationId());
    }
}
