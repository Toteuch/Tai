// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport.debug;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final TaiEventPublisher eventPublisher;

    public DebugController(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/text")
    public String sendText(@RequestParam String text) {
        eventPublisher.publish(
                new UiManualTextInputReceivedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        UUID.randomUUID().toString(),
                        EventSource.UI,
                        text));
        return "OK";
    }
}
