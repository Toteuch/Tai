package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.internal.AssistantSpeechStartedEvent;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.session.SpeakingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AssistantSpeechStartedEventHandler implements EventHandler<AssistantSpeechStartedEvent> {
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");

    private final SessionStore sessionStore;

    public AssistantSpeechStartedEventHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_SPEECH_STARTED;
    }

    @Override
    public void handle(AssistantSpeechStartedEvent event) {
        SessionContext sessionContext = sessionStore.get();

        sessionContext.setSpeakingState(SpeakingState.SPEAKING);
        sessionContext.getActiveTurn().setAssistantPlaybackStarted(true);
    }
}
