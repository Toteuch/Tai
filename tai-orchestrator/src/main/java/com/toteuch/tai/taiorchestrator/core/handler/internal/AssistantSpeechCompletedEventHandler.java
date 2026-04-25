package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.internal.AssistantSpeechCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.session.SpeakingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AssistantSpeechCompletedEventHandler implements EventHandler<AssistantSpeechCompletedEvent> {
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public AssistantSpeechCompletedEventHandler(
        SessionStore sessionStore,
        TaiEventPublisher eventPublisher
    ) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_SPEECH_COMPLETED;
    }

    @Override
    public void handle(AssistantSpeechCompletedEvent event) {
        SessionContext sessionContext = sessionStore.get();

        sessionContext.setSpeakingState(SpeakingState.SILENT);
        sessionContext.getActiveTurn().setAssistantPlaybackCompleted(true);

        eventPublisher.publish(new ConversationTurnCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            event.correlationId(),
            EventSource.ORCHESTRATOR
        ));
    }
}
