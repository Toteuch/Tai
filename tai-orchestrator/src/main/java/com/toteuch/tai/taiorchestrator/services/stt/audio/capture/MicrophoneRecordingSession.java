package com.toteuch.tai.taiorchestrator.services.stt.audio.capture;

import javax.sound.sampled.TargetDataLine;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrophoneRecordingSession {

    private final String correlationId;
    private final Path outputFile;
    private final TargetDataLine line;
    private final Future<?> captureTask;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public MicrophoneRecordingSession(
        String correlationId,
        Path outputFile,
        TargetDataLine line,
        Future<?> captureTask
    ) {
        this.correlationId = correlationId;
        this.outputFile = outputFile;
        this.line = line;
        this.captureTask = captureTask;
    }

    public String getCorrelationId() {
        return correlationId;
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
