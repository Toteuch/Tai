// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport;

import com.toteuch.tai.events.ui.UiStopSpeakReceivedEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/ui")
public class UiEventController {
    private final TaiEventPublisher eventPublisher;

    public UiEventController(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/stop-speak")
    public void onStopSpeak(@RequestBody UiStopSpeakReceivedEvent event) {
        eventPublisher.publish(event);
    }
}
