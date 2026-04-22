package com.toteuch.tai.taiorchestrator.services.audio.capture;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class MicrophoneCaptureService {

    private static final Logger log = LoggerFactory.getLogger(MicrophoneCaptureService.class);

    private final MicrophoneCaptureProperties properties;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, MicrophoneRecordingSession> activeSessions = new ConcurrentHashMap<>();

    public MicrophoneCaptureService(MicrophoneCaptureProperties properties) {
        this.properties = properties;
    }

    public boolean isRecording(String sessionId) {
        MicrophoneRecordingSession session = activeSessions.get(sessionId);
        return session != null && session.isActive();
    }

    public Path startRecording(String sessionId) throws Exception {
        if (isRecording(sessionId)) {
            throw new IllegalStateException("A microphone recording is already active for sessionId=" + sessionId);
        }

        Files.createDirectories(Path.of(properties.getOutputDir()));

        AudioFormat format = new AudioFormat(
            properties.getSampleRate(),
            properties.getSampleSizeBits(),
            properties.getChannels(),
            properties.isSigned(),
            properties.isBigEndian()
        );

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new IllegalStateException("Microphone line is not supported for format: " + format);
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        Path outputFile = buildOutputPath(sessionId);

        Future<?> captureTask = executorService.submit(() -> {
            try (AudioInputStream audioInputStream = new AudioInputStream(line)) {
                log.info("Microphone capture started | sessionId={} outputFile={}", sessionId, outputFile);
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile.toFile());
                log.info("Microphone capture finished | sessionId={} outputFile={}", sessionId, outputFile);
            } catch (IOException e) {
                log.error("Microphone capture failed | sessionId={} outputFile={}", sessionId, outputFile, e);
            }
        });

        MicrophoneRecordingSession session = new MicrophoneRecordingSession(
            sessionId,
            outputFile,
            line,
            captureTask
        );

        activeSessions.put(sessionId, session);
        return outputFile;
    }

    public MicrophoneStopResult stopRecording(String sessionId) {
        MicrophoneRecordingSession session = activeSessions.remove(sessionId);
        if (session == null) {
            return new MicrophoneStopResult(
                false,
                null,
                "MIC_NOT_RECORDING",
                "No active microphone recording for sessionId=" + sessionId
            );
        }

        session.deactivate();

        try {
            TargetDataLine line = session.getLine();
            if (line != null) {
                try {
                    line.stop();
                } catch (Exception ignored) {
                }
                try {
                    line.close();
                } catch (Exception ignored) {
                }
            }

            Future<?> captureTask = session.getCaptureTask();
            if (captureTask != null) {
                try {
                    captureTask.get(3, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    captureTask.cancel(true);
                }
            }

            Path audioFile = session.getOutputFile();
            if (audioFile == null || !Files.exists(audioFile)) {
                return new MicrophoneStopResult(
                    false,
                    null,
                    "MIC_OUTPUT_MISSING",
                    "Recording stopped but no WAV file was produced"
                );
            }

            return new MicrophoneStopResult(
                true,
                audioFile,
                null,
                null
            );

        } catch (Exception e) {
            log.error("Microphone stop failed | sessionId={}", sessionId, e);
            return new MicrophoneStopResult(
                false,
                null,
                "MIC_STOP_ERROR",
                e.getMessage()
            );
        }
    }

    private Path buildOutputPath(String sessionId) {
        String fileName = "mic_" + sessionId + "_" + System.currentTimeMillis() + ".wav";
        return Path.of(properties.getOutputDir(), fileName);
    }

    @PreDestroy
    public void shutdown() {
        for (String sessionId : activeSessions.keySet()) {
            stopRecording(sessionId);
        }
        executorService.shutdownNow();
    }
}
