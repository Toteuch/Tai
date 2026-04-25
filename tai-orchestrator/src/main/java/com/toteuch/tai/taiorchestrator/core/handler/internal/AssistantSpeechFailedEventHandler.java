package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.internal.AssistantSpeechFailedEvent;
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
public class AssistantSpeechFailedEventHandler implements EventHandler<AssistantSpeechFailedEvent> {
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;

    public AssistantSpeechFailedEventHandler(
        SessionStore sessionStore,
        TaiEventPublisher eventPublisher
    ) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public EventType supports() {
        return EventType.ASSISTANT_SPEECH_FAILED;
    }

    @Override
    public void handle(AssistantSpeechFailedEvent event) {
        SessionContext sessionContext = sessionStore.get();
        errorLog.error("TTS speech failed | correlationId={} errorCode={} errorMessage={}",
            event.correlationId(),
            event.errorCode(),
            event.errorMessage()
        );

        sessionContext.setSpeakingState(SpeakingState.SILENT);

        eventPublisher.publish(new ConversationTurnCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            event.correlationId(),
            EventSource.ORCHESTRATOR
        ));
    }
}
