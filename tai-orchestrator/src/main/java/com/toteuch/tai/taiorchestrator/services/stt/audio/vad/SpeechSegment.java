package com.toteuch.tai.taiorchestrator.services.stt.audio.vad;

import java.nio.file.Path;

public record SpeechSegment(
    long durationMs,
    double averageEnergy,
    Path audioFile
) {
}
