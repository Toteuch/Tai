package com.toteuch.tai.orchestrator.transport.debug;

import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiManualTextInputReceivedEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final TaiEventPublisher eventPublisher;

    public DebugController(
        TaiEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/text")
    public String sendText(@RequestParam String text) {
        eventPublisher.publish(new UiManualTextInputReceivedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            UUID.randomUUID().toString(),
            EventSource.UI,
            text
        ));
        return "OK";
    }
}
