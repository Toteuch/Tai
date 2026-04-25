package com.toteuch.tai.orchestrator.transport;

import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.transport.events.llm.LlmResponseCompletedEventRequest;
import com.toteuch.tai.orchestrator.transport.events.llm.LlmResponseFailedEventRequest;
import com.toteuch.tai.orchestrator.transport.events.llm.LlmTransportEventMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/llm")
public class LlmEventController {
    private final TaiEventPublisher eventPublisher;
    private final LlmTransportEventMapper mapper;

    public LlmEventController(
        TaiEventPublisher eventPublisher,
        LlmTransportEventMapper mapper
    ) {
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @PostMapping("/response-completed")
    public void onResponseCompleted(@RequestBody LlmResponseCompletedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }

    @PostMapping("/response-failed")
    public void onResponseFailed(@RequestBody LlmResponseFailedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }
}
