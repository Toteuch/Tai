package com.toteuch.tai.taiorchestrator.transport.events.tts;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.taiorchestrator.transport.events.AbstractTransportEventMapper;
import org.springframework.stereotype.Component;

@Component
public class TtsTransportEventMapper extends AbstractTransportEventMapper {
    public TaiEvent toEvent(TtsPlaybackCompletedEventRequest req) {
        return new TtsPlaybackCompletedEvent(
            safeId(req.getEventId()),
            safeTime(req.getCreatedAt()),
            safeCorrelation(req.getCorrelationId()),
            EventSource.TTS_SERVICE,
            req.getText(),
            req.getSpeechDurationMs()
        );
    }

    public TaiEvent toEvent(TtsPlaybackStartedEventRequest req) {
        return new TtsPlaybackStartedEvent(
            safeId(req.getEventId()),
            safeTime(req.getCreatedAt()),
            safeCorrelation(req.getCorrelationId()),
            EventSource.TTS_SERVICE,
            req.getText(),
            req.getVoiceId()
        );
    }

    public TaiEvent toEvent(TtsPlaybackFailedEventRequest req) {
        return new TtsPlaybackFailedEvent(
            safeId(req.getEventId()),
            safeTime(req.getCreatedAt()),
            safeCorrelation(req.getCorrelationId()),
            EventSource.TTS_SERVICE,
            req.getErrorCode(),
            req.getErrorMessage()
        );
    }
}
