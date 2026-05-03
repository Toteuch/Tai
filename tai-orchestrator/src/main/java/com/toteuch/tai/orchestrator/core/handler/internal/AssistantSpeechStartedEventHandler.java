// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshReason;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshRequester;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import org.springframework.stereotype.Component;

@Component
public class AssistantSpeechStartedEventHandler
        implements EventHandler<AssistantSpeechStartedEvent> {

    private final SessionStore sessionStore;
    private final ModuleRuntimeUpdater runtimeUpdater;
    private final UiStateRefreshRequester uiStateRefreshRequester;

    public AssistantSpeechStartedEventHandler(
            SessionStore sessionStore,
            ModuleRuntimeUpdater runtimeUpdater,
            UiStateRefreshRequester uiStateRefreshRequester) {
        this.sessionStore = sessionStore;
        this.runtimeUpdater = runtimeUpdater;
        this.uiStateRefreshRequester = uiStateRefreshRequester;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_SPEECH_STARTED;
    }

    @Override
    public void handle(AssistantSpeechStartedEvent event) {
        SessionContext sessionContext = sessionStore.get();
        sessionContext
                .getTurnMetrics(event.correlationId())
                .setTtsSpeechStartAt(event.occurredAt());
        sessionContext
                .getTurnMetrics(event.correlationId())
                .setTtsSynthesisMs(event.synthesisDurationMs());

        sessionContext.setSpeakingState(SpeakingState.SPEAKING);
        sessionContext.getActiveTurn().setAssistantPlaybackStarted(true);
        runtimeUpdater.ttsSpeaking(event.correlationId());

        uiStateRefreshRequester.requestRefresh(
                UiStateRefreshReason.RUNTIME_EVENT, event.correlationId());
    }
}
