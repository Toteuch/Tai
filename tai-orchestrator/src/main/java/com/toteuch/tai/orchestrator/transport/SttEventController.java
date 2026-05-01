// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport;

import com.toteuch.tai.events.stt.SttSpeechStartedEvent;
import com.toteuch.tai.events.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.events.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.events.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/stt")
public class SttEventController {

    private final TaiEventPublisher eventPublisher;

    public SttEventController(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/transcript-accepted")
    public void onTranscriptAccepted(@RequestBody SttTranscriptAcceptedEvent event) {
        eventPublisher.publish(event);
    }

    @PostMapping("/transcript-unintelligible")
    public void onTranscriptUnintelligible(@RequestBody SttTranscriptUnintelligibleEvent event) {
        eventPublisher.publish(event);
    }

    @PostMapping("/transcript-noise")
    public void onTranscriptNoise(@RequestBody SttTranscriptNoiseEvent event) {
        eventPublisher.publish(event);
    }

    @PostMapping("/speech-started")
    public void onSpeechStarted(@RequestBody SttSpeechStartedEvent event) {
        eventPublisher.publish(event);
    }
}
