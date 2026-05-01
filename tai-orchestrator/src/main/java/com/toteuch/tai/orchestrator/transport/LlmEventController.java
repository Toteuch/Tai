// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport;

import com.toteuch.tai.events.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.events.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/llm")
public class LlmEventController {
    private final TaiEventPublisher eventPublisher;

    public LlmEventController(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/response-completed")
    public void onResponseCompleted(@RequestBody LlmResponseCompletedEvent event) {
        eventPublisher.publish(event);
    }

    @PostMapping("/response-failed")
    public void onResponseFailed(@RequestBody LlmResponseFailedEvent event) {
        eventPublisher.publish(event);
    }
}
