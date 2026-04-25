package com.toteuch.tai.taiorchestrator.services.stt.audio.capture;

import javax.sound.sampled.TargetDataLine;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrophoneRecordingSession {

    private final String sessionId;
    private final Path outputFile;
    private final TargetDataLine line;
    private final Future<?> captureTask;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public MicrophoneRecordingSession(
        String sessionId,
        Path outputFile,
        TargetDataLine line,
        Future<?> captureTask
    ) {
        this.sessionId = sessionId;
        this.outputFile = outputFile;
        this.line = line;
        this.captureTask = captureTask;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public TargetDataLine getLine() {
        return line;
    }

    public Future<?> getCaptureTask() {
        return captureTask;
    }

    public boolean isActive() {
        return active.get();
    }

    public void deactivate() {
        active.set(false);
    }
}
