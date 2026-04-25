package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.EventHandler;
import com.toteuch.tai.taiorchestrator.core.UserInputProcessor;
import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.inbound.UiManualTextInputReceivedEvent;
import org.springframework.stereotype.Component;

@Component
public class UiManualTextInputReceivedEventHandler implements EventHandler<UiManualTextInputReceivedEvent> {

    private final UserInputProcessor userInputProcessor;

    public UiManualTextInputReceivedEventHandler(UserInputProcessor userInputProcessor) {
        this.userInputProcessor = userInputProcessor;
    }

    @Override
    public EventType supports() {
        return EventType.UI_MANUAL_TEXT_INPUT_RECEIVED;
    }

    @Override
    public void handle(UiManualTextInputReceivedEvent event) {
        userInputProcessor.processUserText(
            event.sessionId(),
            event.correlationId(),
            event.text(),
            false
        );
    }
}
