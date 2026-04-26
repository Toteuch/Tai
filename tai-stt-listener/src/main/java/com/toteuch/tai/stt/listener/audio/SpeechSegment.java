package com.toteuch.tai.stt.listener.audio;

import java.nio.file.Path;

public record SpeechSegment(
        Path audioFile,
        long durationMs,
        double averageEnergy,
        double peakEnergy,
        double voicedRatio,
        boolean speechStarted,
        boolean speechEnded) {}
