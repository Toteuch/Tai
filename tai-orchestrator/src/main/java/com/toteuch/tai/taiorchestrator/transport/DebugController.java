package com.toteuch.tai.taiorchestrator.transport;

import com.toteuch.tai.taiorchestrator.core.OrchestratorEngine;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.UiManualTextInputReceivedEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final OrchestratorEngine orchestratorEngine;

    public DebugController(OrchestratorEngine orchestratorEngine) {
        this.orchestratorEngine = orchestratorEngine;
    }

    @PostMapping("/text")
    public String sendText(@RequestParam String sessionId, @RequestParam String text) {
        UiManualTextInputReceivedEvent event = new UiManualTextInputReceivedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            sessionId,
            UUID.randomUUID().toString(),
            EventSource.UI,
            text
        );

        orchestratorEngine.handle(event);
        return "OK";
    }
}
