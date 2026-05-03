// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.transport;

import com.toteuch.tai.events.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.events.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.events.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("events/tts")
public class TtsEventController {
    private final TaiEventPublisher eventPublisher;

    public TtsEventController(TaiEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/playback-started")
    public void onPlaybackStarted(@RequestBody TtsPlaybackStartedEvent event) {
        eventPublisher.publish(event);
    }

    @PostMapping("/playback-completed")
    public void onPlaybackCompleted(@RequestBody TtsPlaybackCompletedEvent event) {
        eventPublisher.publish(event);
    }

    @PostMapping("/playback-failed")
    public void onPlaybackFailed(@RequestBody TtsPlaybackFailedEvent event) {
        eventPublisher.publish(event);
    }
}
