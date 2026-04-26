package com.toteuch.tai.stt.listener.api.dto;

import com.toteuch.tai.stt.listener.audio.SpeechSegment;

public record SpeechSegmentResponse(
    String audioFile,
    long durationMs,
    double averageEnergy,
    double peakEnergy,
    double voicedRatio,
    boolean speechStarted,
    boolean speechEnded
) {
    public static SpeechSegmentResponse from(SpeechSegment segment) {
        return new SpeechSegmentResponse(
            segment.audioFile().toString(),
            segment.durationMs(),
            segment.averageEnergy(),
            segment.peakEnergy(),
            segment.voicedRatio(),
            segment.speechStarted(),
            segment.speechEnded()
        );
    }
}
