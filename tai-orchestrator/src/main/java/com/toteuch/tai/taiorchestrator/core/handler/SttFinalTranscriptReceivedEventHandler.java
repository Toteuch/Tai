package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.UserInputProcessor;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.SttFinalTranscriptReceivedEvent;
import org.springframework.stereotype.Component;

@Component
public class SttFinalTranscriptReceivedEventHandler implements EventHandler<SttFinalTranscriptReceivedEvent> {

    private final UserInputProcessor userInputProcessor;

    public SttFinalTranscriptReceivedEventHandler(UserInputProcessor userInputProcessor) {
        this.userInputProcessor = userInputProcessor;
    }

    @Override
    public EventType supports() {
        return EventType.STT_FINAL_TRANSCRIPT_RECEIVED;
    }

    @Override
    public void handle(SttFinalTranscriptReceivedEvent event) {
        userInputProcessor.processUserText(
            event.sessionId(),
            event.correlationId(),
            event.transcript(),
            event.interruption()
        );
    }
}
