// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import com.toteuch.tai.orchestrator.services.llm.LlmClient;
import com.toteuch.tai.orchestrator.services.llm.LlmMessage;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import com.toteuch.tai.orchestrator.support.ContextAssembler;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserUtteranceAcceptedEventHandler implements EventHandler<UserUtteranceAcceptedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final ContextAssembler contextAssembler;
    private final LlmClient llmClient;

    public UserUtteranceAcceptedEventHandler(
            SessionStore sessionStore, ContextAssembler contextAssembler, LlmClient llmClient) {
        this.sessionStore = sessionStore;
        this.contextAssembler = contextAssembler;
        this.llmClient = llmClient;
    }

    @Override
    public EventType supports() {
        return EventType.USER_UTTERANCE_ACCEPTED;
    }

    @Override
    public void handle(UserUtteranceAcceptedEvent event) {

        SessionContext sessionContext = sessionStore.get();

        sessionContext
                .getTurnMetrics(event.correlationId())
                .setUserUtteranceAcceptedAt(event.occurredAt());
        sessionContext
                .getTurnMetrics(event.correlationId())
                .setTranscriptDurationMs(event.transcriptDurationMs());
        sessionContext
                .getTurnMetrics(event.correlationId())
                .setUserSpeechDurationMs(event.speechDurationMs());

        String normalizedText = normalizeTaiName(event.text());

        ConversationTurn newTurn =
                new ConversationTurn(event.correlationId(), normalizedText, Instant.now(), true);
        sessionContext.setActiveTurn(newTurn);

        List<LlmMessage> messages =
                contextAssembler.assemble(sessionContext, normalizedText, false);
        sessionContext.setThinkingState(ThinkingState.GENERATING);
        perfLog.debug("LLM generation called | correlationId={}", event.correlationId());
        llmClient.generateReply(sessionContext.getActiveTurn().getCorrelationId(), messages);
    }

    private String normalizeTaiName(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        return text.replace("Ty", "Tai").replace("Thaï", "Tai").replace("Thai", "Tai");
    }
}
