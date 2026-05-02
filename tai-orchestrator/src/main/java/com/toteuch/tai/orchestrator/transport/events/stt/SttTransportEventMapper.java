package com.toteuch.tai.orchestrator.transport.events.stt;

import com.toteuch.tai.orchestrator.events.TaiEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttSpeechStartedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.orchestrator.events.inbound.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.orchestrator.transport.events.AbstractTransportEventMapper;
import org.springframework.stereotype.Component;

@Component
public class SttTransportEventMapper extends AbstractTransportEventMapper {

    public TaiEvent toEvent(SttSpeechStartedEventRequest req) {
        return new SttSpeechStartedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getDurationMs(),
                req.getAverageEnergy());
    }

    public TaiEvent toEvent(SttTranscriptAcceptedEventRequest req) {
        return new SttTranscriptAcceptedEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getText(),
                req.getLanguage(),
                req.getLanguageProbability(),
                req.getDurationMs(),
                req.getAverageEnergy(),
                req.getReason(),
                req.getSuspicionScore(),
                req.getTranscriptionDurationMs());
    }

    public TaiEvent toEvent(SttTranscriptUnintelligibleEventRequest req) {
        return new SttTranscriptUnintelligibleEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getLanguage(),
                req.getLanguageProbability(),
                req.getDurationMs(),
                req.getAverageEnergy(),
                req.getReason(),
                req.getSuspicionScore(),
                req.getTranscriptionDurationMs());
    }

    public TaiEvent toEvent(SttTranscriptNoiseEventRequest req) {
        return new SttTranscriptNoiseEvent(
                safeId(req.getEventId()),
                safeTime(req.getCreatedAt()),
                safeCorrelation(req.getCorrelationId()),
                mapEventSource(req.getSource()),
                req.getDurationMs(),
                req.getAverageEnergy(),
                req.getReason(),
                req.getSuspicionScore(),
                req.getTranscriptionDurationMs());
    }
}
