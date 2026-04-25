package com.toteuch.tai.taiorchestrator.transport.stt;

import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttSpeechStartedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTranscriptAcceptedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTranscriptNoiseEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTranscriptUnintelligibleEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTransportEventMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/events/stt")
public class InternalSttEventController {

    private final TaiEventPublisher eventPublisher;
    private final SttTransportEventMapper mapper;

    public InternalSttEventController(
        TaiEventPublisher eventPublisher,
        SttTransportEventMapper mapper
    ) {
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @PostMapping("/transcript-accepted")
    public void onTranscriptAccepted(@RequestBody SttTranscriptAcceptedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }

    @PostMapping("/transcript-unintelligible")
    public void onTranscriptUnintelligible(@RequestBody SttTranscriptUnintelligibleEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }

    @PostMapping("/transcript-noise")
    public void onTranscriptNoise(@RequestBody SttTranscriptNoiseEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }

    @PostMapping("/speech-started")
    public void onSpeechStarted(@RequestBody SttSpeechStartedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }
}
