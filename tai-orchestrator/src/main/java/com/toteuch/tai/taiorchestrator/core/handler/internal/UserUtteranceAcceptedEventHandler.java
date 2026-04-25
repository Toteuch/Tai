package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.llm.LlmResponseFailedEvent;
import com.toteuch.tai.taiorchestrator.events.internal.UserUtteranceAcceptedEvent;
import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.session.ThinkingState;
import com.toteuch.tai.taiorchestrator.support.ContextAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class UserUtteranceAcceptedEventHandler implements EventHandler<UserUtteranceAcceptedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final SessionStore sessionStore;
    private final TtsClient ttsClient;
    private final TaiEventPublisher eventPublisher;
    private final ContextAssembler contextAssembler;
    private final LlmClient llmClient;

    public UserUtteranceAcceptedEventHandler(
        SessionStore sessionStore,
        TtsClient ttsClient,
        TaiEventPublisher eventPublisher,
        ContextAssembler contextAssembler,
        LlmClient llmClient
    ) {
        this.sessionStore = sessionStore;
        this.ttsClient = ttsClient;
        this.eventPublisher = eventPublisher;
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

        ConversationTurn newTurn = new ConversationTurn(event.correlationId(), event.text(), Instant.now(), true);
        sessionContext.setActiveTurn(newTurn);

        List<LlmMessage> messages = contextAssembler.assemble(sessionContext, event.text(), false);
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

    private void supersedePreviousTurnIfNeeded(SessionContext sessionContext, String newCorrelationId) {
        ConversationTurn activeTurn = sessionContext.getActiveTurn();
        if (activeTurn == null) {
            return;
        }

        if (newCorrelationId.equals(activeTurn.getCorrelationId())) {
            return;
        }

        boolean noAssistantReplyYet = !activeTurn.isAssistantReplyGenerated();
        boolean assistantWasSpeaking = activeTurn.isAssistantPlaybackStarted() && !activeTurn.isAssistantPlaybackCompleted();

        if (noAssistantReplyYet) {
            activeTurn.setSupersededBeforeAssistantReply(true);
            activeTurn.setSupersededByCorrelationId(newCorrelationId);
        }

        if (assistantWasSpeaking) {
            activeTurn.setAssistantPlaybackInterrupted(true);
            activeTurn.setSupersededByCorrelationId(newCorrelationId);
        }
    }
}
