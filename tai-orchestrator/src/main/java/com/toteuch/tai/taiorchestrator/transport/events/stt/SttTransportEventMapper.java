package com.toteuch.tai.taiorchestrator.transport.events.stt;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.stt.SttSpeechStartedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.taiorchestrator.transport.events.AbstractTransportEventRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SttTransportEventMapper {

    public TaiEvent toEvent(SttSpeechStartedEventRequest req) {
        return new SttSpeechStartedEvent(
            safeId(req.getEventId()),
            safeTime(req.getTimestamp()),
            safeCorrelation(req),
            EventSource.STT_SERVICE,
            req.getDurationMs(),
            req.getAverageEnergy()
        );
    }

    public TaiEvent toEvent(SttTranscriptAcceptedEventRequest req) {
        return new SttTranscriptAcceptedEvent(
            safeId(req.getEventId()),
            safeTime(req.getTimestamp()),
            safeCorrelation(req),
            EventSource.TTS_SERVICE,
            req.getText(),
            req.getLanguage(),
            req.getLanguageProbability(),
            req.getDurationMs(),
            req.getAverageEnergy(),
            req.getReason(),
            req.getSuspicionScore()
        );
    }

    public TaiEvent toEvent(SttTranscriptUnintelligibleEventRequest req) {
        return new SttTranscriptUnintelligibleEvent(
            safeId(req.getEventId()),
            safeTime(req.getTimestamp()),
            safeCorrelation(req),
            EventSource.TTS_SERVICE,
            req.getLanguage(),
            req.getLanguageProbability(),
            req.getDurationMs(),
            req.getAverageEnergy(),
            req.getReason(),
            req.getSuspicionScore()
        );
    }

    public TaiEvent toEvent(SttTranscriptNoiseEventRequest req) {
        return new SttTranscriptNoiseEvent(
            safeId(req.getEventId()),
            safeTime(req.getTimestamp()),
            safeCorrelation(req),
            EventSource.TTS_SERVICE,
            req.getDurationMs(),
            req.getAverageEnergy(),
            req.getReason(),
            req.getSuspicionScore()
        );
    }

    private String safeId(String id) {
        return id != null ? id : UUID.randomUUID().toString();
    }

    private Instant safeTime(Instant t) {
        return t != null ? t : Instant.now();
    }

    private String safeCorrelation(AbstractTransportEventRequest req) {
        return req.getCorrelationId() != null
            ? req.getCorrelationId()
            : safeId(req.getEventId());
    }
}
