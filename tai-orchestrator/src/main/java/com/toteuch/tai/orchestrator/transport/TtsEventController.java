package com.toteuch.tai.orchestrator.transport;

import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.transport.events.tts.TtsPlaybackCompletedEventRequest;
import com.toteuch.tai.orchestrator.transport.events.tts.TtsPlaybackFailedEventRequest;
import com.toteuch.tai.orchestrator.transport.events.tts.TtsPlaybackStartedEventRequest;
import com.toteuch.tai.orchestrator.transport.events.tts.TtsTransportEventMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("events/tts")
public class TtsEventController {
    private final TaiEventPublisher eventPublisher;
    private final TtsTransportEventMapper mapper;

    public TtsEventController(TaiEventPublisher eventPublisher, TtsTransportEventMapper mapper) {
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @PostMapping("/playback-started")
    public void onPlaybackStarted(@RequestBody TtsPlaybackStartedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }

    @PostMapping("/playback-completed")
    public void onPlaybackCompleted(@RequestBody TtsPlaybackCompletedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }

    @PostMapping("/playback-failed")
    public void onPlaybackFailed(@RequestBody TtsPlaybackFailedEventRequest request) {
        eventPublisher.publish(mapper.toEvent(request));
    }
}
