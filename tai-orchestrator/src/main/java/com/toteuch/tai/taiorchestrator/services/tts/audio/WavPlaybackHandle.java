package com.toteuch.tai.taiorchestrator.services.tts.audio;

import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

public class WavPlaybackHandle {

    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile SourceDataLine line;

    public boolean isStopRequested() {
        return stopRequested.get();
    }

    public void requestStop() {
        stopRequested.set(true);

        SourceDataLine currentLine = this.line;
        if (currentLine != null) {
            try {
                currentLine.stop();
            } catch (Exception ignored) {
            }
            try {
                currentLine.flush();
            } catch (Exception ignored) {
            }
            try {
                currentLine.close();
            } catch (Exception ignored) {
            }
        }
    }

    public SourceDataLine getLine() {
        return line;
    }

    public void setLine(SourceDataLine line) {
        this.line = line;
    }
}
