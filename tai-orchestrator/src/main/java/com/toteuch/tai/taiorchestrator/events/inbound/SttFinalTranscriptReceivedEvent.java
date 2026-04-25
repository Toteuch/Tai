package com.toteuch.tai.taiorchestrator.events.inbound;

import com.toteuch.tai.taiorchestrator.events.EventType;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.events.EventSource;

import java.time.Instant;
/**
 * Event emitted when the STT service has produced a final user transcript
 * that can be treated as a stable spoken input.
 *
 * <p>This event is the main voice-input entry point of V1. It marks the
 * transition from raw speech processing to actual conversational handling
 * by the orchestrator.</p>
 *
 * @param transcript the final transcribed user utterance
 * @param language the detected language of the utterance, if available
 * @param confidence the confidence score returned by the STT service, if available
 * @param interruption indicates whether this transcript should be treated as an interruption
 *                     of an already running assistant reply
 */
public record SttFinalTranscriptReceivedEvent(
    String eventId,
    Instant occurredAt,
    String sessionId,
    String correlationId,
    EventSource source,
    String transcript,
    String language,
    Double confidence,
    boolean interruption
) implements TaiEvent {

    @Override
    public EventType type() {
        return EventType.STT_FINAL_TRANSCRIPT_RECEIVED;
    }
}
