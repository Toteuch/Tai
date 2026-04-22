package com.toteuch.tai.taiorchestrator.services.audio.capture;

import java.nio.file.Path;

public record MicrophoneStopResult(
    boolean success,
    Path audioFile,
    String errorCode,
    String errorMessage
) {
}
