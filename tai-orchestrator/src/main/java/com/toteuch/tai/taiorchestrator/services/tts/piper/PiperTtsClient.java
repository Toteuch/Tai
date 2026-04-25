package com.toteuch.tai.taiorchestrator.services.tts.piper;

import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.services.tts.audio.JavaAudioPlaybackService;
import com.toteuch.tai.taiorchestrator.services.tts.audio.WavPlaybackHandle;
import com.toteuch.tai.taiorchestrator.transport.TtsEventController;
import com.toteuch.tai.taiorchestrator.transport.events.TransportEventSource;
import com.toteuch.tai.taiorchestrator.transport.events.tts.TtsPlaybackCompletedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.tts.TtsPlaybackFailedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.tts.TtsPlaybackStartedEventRequest;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Primary
public class PiperTtsClient implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(PiperTtsClient.class);

    private final PiperTtsProperties properties;
    private final TtsEventController eventController;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();
    private final JavaAudioPlaybackService audioPlaybackService;
    private final Map<String, WavPlaybackHandle> activePlaybackHandles = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> activeTasks = new ConcurrentHashMap<>();

    public PiperTtsClient(
        PiperTtsProperties properties,
        TtsEventController eventController,
        JavaAudioPlaybackService audioPlaybackService
    ) {
        log.debug("PiperTtsClient initialized");
        this.properties = properties;
        this.eventController = eventController;
        this.audioPlaybackService = audioPlaybackService;
    }

    @Override
    public void speak(String correlationId, String text) {
        log.debug("PiperTtsClient speak called | correlationId={} text={}", correlationId, text);
        stop(correlationId);

        Future<?> future = executorService.submit(() -> {

            try {
                Path outputFile = buildOutputPath(correlationId);
                try {
                    Files.createDirectories(Path.of(properties.getOutputDir()));
                    log.debug("Piper TTS config | executable={} model={} config={} outputDir={} segmentationMode={}",
                        properties.getExecutable(),
                        properties.getModel(),
                        properties.getConfig(),
                        properties.getOutputDir(),
                        properties.getSegmentationMode());

                    synthesizeToWav(correlationId, outputFile, text);

                    postStarted(correlationId, text);
                    long durationMs = playWav(correlationId, outputFile);
                    postCompleted(correlationId, text, durationMs);

                } finally {
                    try {
                        Files.deleteIfExists(outputFile);
                    } catch (IOException e) {
                        log.warn("Could not delete temporary TTS file {}", outputFile, e);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("Piper TTS interrupted | correlationId={}", correlationId);

            } catch (Exception e) {
                log.error("Piper TTS failed | correlationId={}", correlationId, e);
                postFailed(correlationId, "PIPER_TTS_ERROR", e.getMessage());

            } finally {
                activeProcesses.remove(correlationId);
                activeTasks.remove(correlationId);
                activePlaybackHandles.remove(correlationId);
            }
        });

        activeTasks.put(correlationId, future);
    }

    @Override
    public void stop(String sessionId) {
        Future<?> task = activeTasks.remove(sessionId);
        if (task != null) {
            task.cancel(true);
        }

        WavPlaybackHandle playbackHandle = activePlaybackHandles.remove(sessionId);
        if (playbackHandle != null) {
            playbackHandle.requestStop();
        }

        Process process = activeProcesses.remove(sessionId);
        if (process != null) {
            process.destroyForcibly();
        }
    }

    private void synthesizeToWav(String correlationId, Path outputFile, String text) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
            properties.getExecutable(),
            "--model", properties.getModel(),
            "--config", properties.getConfig(),
            "--output_file", outputFile.toString()
        );

        processBuilder.redirectErrorStream(true);

        log.debug("Starting Piper process | command={}", processBuilder.command());

        Process process = processBuilder.start();
        activeProcesses.put(correlationId, process);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(text);
            writer.flush();
        }

        try {
            int exitCode = process.waitFor();
            log.debug("Piper process finished | exitCode={} outputFile={}", exitCode, outputFile);
            log.debug("Generated WAV exists={} size={}",
                Files.exists(outputFile),
                Files.exists(outputFile) ? Files.size(outputFile) : -1);
            if (exitCode != 0) {
                throw new IllegalStateException("Piper exited with code " + exitCode);
            }
        } finally {
            activeProcesses.remove(correlationId, process);
        }
    }

    private long playWav(String sessionId, Path wavPath) throws Exception {
        WavPlaybackHandle playbackHandle = new WavPlaybackHandle();
        activePlaybackHandles.put(sessionId, playbackHandle);

        try {
            return audioPlaybackService.playBlocking(wavPath, playbackHandle);
        } finally {
            activePlaybackHandles.remove(sessionId);
        }
    }

    private void postStarted(String correlationId, String text) {
        TtsPlaybackStartedEventRequest response = new TtsPlaybackStartedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setCorrelationId(correlationId);
        response.setSource(TransportEventSource.TTS_SERVICE);
        response.setText(text);
        response.setVoiceId(properties.getVoiceId());

        eventController.onPlaybackStarted(response);
    }

    private void postCompleted(String correlationId, String text, long durationMs) {
        TtsPlaybackCompletedEventRequest response = new TtsPlaybackCompletedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setCorrelationId(correlationId);
        response.setSource(TransportEventSource.TTS_SERVICE);
        response.setText(text);
        response.setSpeechDurationMs(durationMs);

        eventController.onPlaybackCompleted(response);
    }

    private void postFailed(String correlationId, String errorCode, String errorMessage) {
        TtsPlaybackFailedEventRequest response = new TtsPlaybackFailedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setCorrelationId(correlationId);
        response.setSource(TransportEventSource.TTS_SERVICE);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);

        eventController.onPlaybackFailed(response);
    }

    private Path buildOutputPath(String correlationId) {
        String fileName = "tts_" + correlationId + ".wav";
        return Path.of(properties.getOutputDir(), fileName);
    }

    @PreDestroy
    public void shutdown() {
        for (String sessionId : activeTasks.keySet()) {
            stop(sessionId);
        }
        executorService.shutdownNow();
    }
}
