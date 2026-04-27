package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.internal.ClarificationRequestedEvent;
import com.toteuch.tai.orchestrator.services.llm.LlmClient;
import com.toteuch.tai.orchestrator.services.llm.LlmMessage;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClarificationRequestedEventHandler
        implements EventHandler<ClarificationRequestedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final LlmClient llmClient;

    public ClarificationRequestedEventHandler(SessionStore sessionStore, LlmClient llmClient) {
        this.sessionStore = sessionStore;
        this.llmClient = llmClient;
    }

    @Override
    public EventType supports() {
        return EventType.CLARIFICATION_REQUESTED;
    }

    @Override
    public void handle(ClarificationRequestedEvent event) {
        SessionContext sessionContext = sessionStore.get();

        sessionContext
                .getTurnMetrics(event.correlationId())
                .setUserUtteranceAcceptedAt(event.occurredAt());
        sessionContext
                .getTurnMetrics(event.correlationId())
                .setTranscriptDurationMs(event.transcriptDurationMs());
        // This turn mustn't be added in SessionContext.turns, to not be added in the conversation
        // history
        ConversationTurn newTurn =
                new ConversationTurn(event.correlationId(), "...", Instant.now(), false);
        sessionContext.setActiveTurn(newTurn);
        List<LlmMessage> messages =
                List.of(
                        new LlmMessage(
                                "system",
                                "You are Tai. You are having a real-time voice conversation with the user. "
                                        + "The user just spoke, but you could not clearly understand what they said. "
                                        + "Respond naturally, in character, and briefly. "
                                        + "Do not mention transcription, errors, or technical issues. "
                                        + "Do not assume what the user meant. "
                                        + "Ask them to repeat in a casual, natural way. "
                                        + "Avoid formal or overly polite phrasing. "
                                        + "Keep it short and suitable for spoken dialogue."),
                        // This second message is only exists to trigger the LLM to generate a
                        // response, fails otherwise
                        new LlmMessage("user", "..."));
        sessionContext.setThinkingState(ThinkingState.GENERATING);
        perfLog.debug("LLM generation called | correlationId={}", event.correlationId());
        llmClient.generateReply(sessionContext.getActiveTurn().getCorrelationId(), messages);
    }
}
