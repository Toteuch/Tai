package com.toteuch.tai.taiorchestrator.services.stt.audio.capture;

import com.toteuch.tai.taiorchestrator.services.stt.SttClient;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private final SttClient sttClient;

    public MicrophoneCaptureService(
        MicrophoneCaptureProperties properties,
        SttClient sttClient
    ) {
        this.properties = properties;
        this.sttClient = sttClient;
    }

    public boolean isRecording(String correlationId) {
        MicrophoneRecordingSession session = activeSessions.get(correlationId);
        return session != null && session.isActive();
    }

    public Path startRecording(String correlationId) throws Exception {
        if (isRecording(correlationId)) {
            throw new IllegalStateException("A microphone recording is already active for correlationId=" + correlationId);
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

        Path outputFile = buildOutputPath(correlationId);

        Future<?> captureTask = executorService.submit(() -> {
            try (AudioInputStream audioInputStream = new AudioInputStream(line)) {
                log.debug("Microphone capture started | correlationId={} outputFile={}", correlationId, outputFile);
                long silenceStart = -1;
                long recordingStart = System.currentTimeMillis();

                byte[] buffer = new byte[properties.getBufferSize()];

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    while (!Thread.currentThread().isInterrupted() && line.isOpen()) {

                        int bytesRead = line.read(buffer, 0, buffer.length);
                        if (bytesRead <= 0) continue;

                        baos.write(buffer, 0, bytesRead);

                        int level = calculateAudioLevel(buffer, bytesRead);

                        long now = System.currentTimeMillis();

                        if (level < properties.getSilenceThreshold()) {
                            if (silenceStart < 0) {
                                silenceStart = now;
                            } else if ((now - silenceStart) > properties.getSilenceDurationMs()
                                && (now - recordingStart) > properties.getMinRecordingMs()) {

                                log.debug("Silence detected, stopping recording | correlationId={}", correlationId);
                                break;
                            }
                        } else {
                            silenceStart = -1;
                        }
                    }

                    byte[] audioData = baos.toByteArray();

                    try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                         AudioInputStream ais = new AudioInputStream(
                             bais,
                             format,
                             audioData.length / format.getFrameSize())) {

                        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile.toFile());
                    }
                }
                log.debug("Microphone capture finished | correlationId={} outputFile={}", correlationId, outputFile);
            } catch (IOException e) {
                log.error("Microphone capture failed | correlationId={} outputFile={}", correlationId, outputFile, e);
            } finally {
                try {
                    line.stop();
                } catch (Exception ignored) {
                }
                try {
                    line.close();
                } catch (Exception ignored) {
                }

                MicrophoneRecordingSession current = activeSessions.get(correlationId);
                if (current != null) {
                    current.deactivate();
                    activeSessions.remove(correlationId, current);
                }
            }
        });

        MicrophoneRecordingSession session = new MicrophoneRecordingSession(
            correlationId,
            outputFile,
            line,
            captureTask
        );

        activeSessions.put(correlationId, session);
        return outputFile;
    }

    public MicrophoneStopResult stopRecording(String correlationId) {
        MicrophoneRecordingSession session = activeSessions.remove(correlationId);
        if (session == null) {
            return new MicrophoneStopResult(
                false,
                null,
                "MIC_NOT_RECORDING",
                "No active microphone recording for correlationId=" + correlationId
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
            log.error("Microphone stop failed | correlationId={}", correlationId, e);
            return new MicrophoneStopResult(
                false,
                null,
                "MIC_STOP_ERROR",
                e.getMessage()
            );
        }
    }

    public void startRecordingAndWaitForSilence(String correlationId) {
        try {
            Path outputFile = startRecording(correlationId);

            MicrophoneRecordingSession session = activeSessions.get(correlationId);
            if (session == null) {
                log.error("Recording session was not created for correlationId={}", correlationId);
                return;
            }

            Future<?> captureTask = session.getCaptureTask();
            if (captureTask != null) {
                captureTask.get();
            }

            if (!Files.exists(outputFile)) {
                log.error("Recording finished but no WAV file was produced for correlationId={}", correlationId);
                return;
            }
            sttClient.transcribe(outputFile);

        } catch (Exception e) {
            log.error("Auto-process microphone capture failed | correlationId={}", correlationId, e);
        }
    }

    private Path buildOutputPath(String correlationId) {
        String fileName = "mic_" + correlationId + "_" + System.currentTimeMillis() + ".wav";
        return Path.of(properties.getOutputDir(), fileName);
    }

    private int calculateAudioLevel(byte[] buffer, int bytesRead) {
        int max = 0;

        for (int i = 0; i < bytesRead - 1; i += 2) {
            int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xff);
            max = Math.max(max, Math.abs(sample));
        }

        return max;
    }

    @PreDestroy
    public void shutdown() {
        for (String correlationId : activeSessions.keySet()) {
            stopRecording(correlationId);
        }
        executorService.shutdownNow();
    }
}
