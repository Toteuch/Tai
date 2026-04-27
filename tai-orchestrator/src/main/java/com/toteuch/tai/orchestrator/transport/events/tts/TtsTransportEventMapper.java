package com.toteuch.tai.orchestrator.transport.events.tts;

import com.toteuch.tai.orchestrator.events.TaiEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.orchestrator.events.inbound.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventMapper;
import org.springframework.stereotype.Component;

@Component
public class TtsTransportEventMapper extends AbstractTransportEventMapper {
    public TaiEvent toEvent(TtsPlaybackCompletedEventRequest req) {
        return new TtsPlaybackCompletedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getText(),
                req.getSpeechDurationMs());
    }

    public TaiEvent toEvent(TtsPlaybackStartedEventRequest req) {
        return new TtsPlaybackStartedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getText(),
                req.getVoiceId(),
                req.getSynthesisDurationMs());
    }

    public TaiEvent toEvent(TtsPlaybackFailedEventRequest req) {
        return new TtsPlaybackFailedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getErrorCode(),
                req.getErrorMessage(),
                req.getSpeechDurationMs());
    }
}
