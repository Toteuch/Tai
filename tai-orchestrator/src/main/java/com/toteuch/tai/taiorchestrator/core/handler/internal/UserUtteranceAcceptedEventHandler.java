package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.internal.UserUtteranceAcceptedEvent;
import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
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
        llmClient.generateReply(sessionContext.getActiveTurn().getCorrelationId(), messages);
    }
}
