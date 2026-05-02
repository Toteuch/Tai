// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.EventType;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.ThinkingState;
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
public class AssistantReplyFailedEventHandler implements EventHandler<AssistantReplyFailedEvent> {
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;
    private final ModuleRuntimeUpdater runtimeUpdate;
    private final UiStateRefreshRequester uiStateRefreshRequester;

    public AssistantReplyFailedEventHandler(
            SessionStore sessionStore,
            TaiEventPublisher eventPublisher,
            ModuleRuntimeUpdater runtimeUpdater,
            UiStateRefreshRequester uiStateRefreshRequester) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
        this.runtimeUpdate = runtimeUpdater;
        this.uiStateRefreshRequester = uiStateRefreshRequester;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_REPLY_FAILED;
    }

    @Override
    public void handle(AssistantReplyFailedEvent event) {
        SessionContext sessionContext = sessionStore.get();
        errorLog.error(
                "LLM reply failed | correlationId={} errorCode={} errorMessage={}",
                event.correlationId(),
                event.errorCode(),
                event.errorMessage());

        sessionContext
                .getTurnMetrics(event.correlationId())
                .setLlmGenerationMs(event.llmGenerationMs());

        sessionContext.setThinkingState(ThinkingState.IDLE);
        runtimeUpdate.llmError();

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
