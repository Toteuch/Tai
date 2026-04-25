package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.taiorchestrator.events.internal.ClarificationRequestedEvent;
import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.session.ThinkingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ClarificationRequestedEventHandler implements EventHandler<ClarificationRequestedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;
    private final TtsClient ttsClient;
    private final LlmClient llmClient;

    public ClarificationRequestedEventHandler(
        SessionStore sessionStore,
        TaiEventPublisher eventPublisher,
        TtsClient ttsClient,
        LlmClient llmClient
    ) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
        this.ttsClient = ttsClient;
        this.llmClient = llmClient;
    }

    @Override
    public EventType supports() {
        return EventType.CLARIFICATION_REQUESTED;
    }

    @Override
    public void handle(ClarificationRequestedEvent event) {
        SessionContext sessionContext = sessionStore.get();

        if (sessionContext.bargeIn(event.correlationId())) {
            String previousCorrelationId = sessionContext.getActiveTurn().getCorrelationId();
            sessionContext.addTurn(sessionContext.getActiveTurn());
            sessionContext.setActiveTurn(null);

            perfLog.info("TTS stop speech called | correlationId={} activeTurnCorrelationId={}",
                event.correlationId(),
                previousCorrelationId
            );
            ttsClient.stop(previousCorrelationId);
        }

        // This turn mustn't be added in SessionContext.turns, to not be added in the conversation history
        ConversationTurn newTurn = new ConversationTurn(event.correlationId(), "...", Instant.now(), false);
        sessionContext.setActiveTurn(newTurn);
        List<LlmMessage> messages = List.of(
            new LlmMessage(
                "system",
                "You are Tai. You are having a real-time voice conversation with the user. " +
                    "The user just spoke, but you could not clearly understand what they said. " +
                    "Respond naturally, in character, and briefly. " +
                    "Do not mention transcription, errors, or technical issues. " +
                    "Do not assume what the user meant. " +
                    "Ask them to repeat in a casual, natural way. " +
                    "Avoid formal or overly polite phrasing. " +
                    "Keep it short and suitable for spoken dialogue."),
            // This second message is only exists to trigger the LLM to generate a response, fails otherwise
            new LlmMessage(
                "user",
                "..."
            )
        );
        sessionContext.setThinkingState(ThinkingState.GENERATING);
        perfLog.info("LLM generation called | correlationId={}", event.correlationId());
        LlmGenerationResult result = llmClient.generateReply(sessionContext.getActiveTurn().getCorrelationId(), messages);
        if (result.success()) {
            eventPublisher.publish(new LlmResponseCompletedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.correlationId(),
                EventSource.LLM_SERVICE,
                result.responseText(),
                result.modelName(),
                result.inputTokens(),
                result.outputTokens(),
                result.generationDurationMs()
            ));
        } else {
            eventPublisher.publish(new LlmResponseFailedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.correlationId(),
                EventSource.LLM_SERVICE,
                result.errorCode(),
                result.errorMessage()
            ));
        }
    }
}
