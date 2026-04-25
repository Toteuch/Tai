package com.toteuch.tai.taiorchestrator.services.tts.piper;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackFailedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackStartedEvent;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.services.tts.audio.JavaAudioPlaybackService;
import com.toteuch.tai.taiorchestrator.services.tts.audio.WavPlaybackHandle;
import com.toteuch.tai.taiorchestrator.support.TtsSentenceSplitter;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final TtsSentenceSplitter ttsSentenceSplitter;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();
    private final JavaAudioPlaybackService audioPlaybackService;
    private final Map<String, WavPlaybackHandle> activePlaybackHandles = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> activeTasks = new ConcurrentHashMap<>();

    public PiperTtsClient(
        PiperTtsProperties properties,
        ApplicationEventPublisher eventPublisher,
        JavaAudioPlaybackService audioPlaybackService,
        TtsSentenceSplitter ttsSentenceSplitter
    ) {
        log.info("PiperTtsClient initialized");
        this.properties = properties;
        this.eventPublisher = eventPublisher;
        this.audioPlaybackService = audioPlaybackService;
        this.ttsSentenceSplitter = ttsSentenceSplitter;
    }

    @Override
    public void speak(String sessionId, String correlationId, String text) {
        log.info("PiperTtsClient speak called | sessionId={} correlationId={} text={}", sessionId, correlationId, text);
        log.info("Current JVM working directory: {}", System.getProperty("user.dir"));
        stop(sessionId);

        Future<?> future = executorService.submit(() -> {

            try {
                Path outputFile = buildOutputPath(sessionId, correlationId);

                try {
                    Files.createDirectories(Path.of(properties.getOutputDir()));

                    log.info("Piper TTS config | executable={} model={} config={} outputDir={} segmentationMode={}",
                        properties.getExecutable(),
                        properties.getModel(),
                        properties.getConfig(),
                        properties.getOutputDir(),
                        properties.getSegmentationMode());

                    synthesizeToWav(sessionId, outputFile, text);

                    publishStarted(sessionId, correlationId, text);
                    long durationMs = playWav(sessionId, outputFile);
                    publishCompleted(sessionId, correlationId, text, durationMs);

                } finally {
                    try {
                        Files.deleteIfExists(outputFile);
                    } catch (IOException e) {
                        log.warn("Could not delete temporary TTS file {}", outputFile, e);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Piper TTS interrupted | sessionId={} correlationId={}", sessionId, correlationId);

            } catch (Exception e) {
                log.error("Piper TTS failed | sessionId={} correlationId={}", sessionId, correlationId, e);
                publishFailed(sessionId, correlationId, "PIPER_TTS_ERROR", e.getMessage());

            } finally {
                activeProcesses.remove(sessionId);
                activeTasks.remove(sessionId);
                activePlaybackHandles.remove(sessionId);
            }
        });

        activeTasks.put(sessionId, future);
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

    private void synthesizeToWav(String sessionId, Path outputFile, String text) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
            properties.getExecutable(),
            "--model", properties.getModel(),
            "--config", properties.getConfig(),
            "--output_file", outputFile.toString()
        );

        processBuilder.redirectErrorStream(true);

        log.info("Starting Piper process | command={}", processBuilder.command());

        Process process = processBuilder.start();
        activeProcesses.put(sessionId, process);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(text);
            writer.flush();
        }

        try {
            int exitCode = process.waitFor();
            log.info("Piper process finished | exitCode={} outputFile={}", exitCode, outputFile);
            log.info("Generated WAV exists={} size={}",
                Files.exists(outputFile),
                Files.exists(outputFile) ? Files.size(outputFile) : -1);
            if (exitCode != 0) {
                throw new IllegalStateException("Piper exited with code " + exitCode);
            }
        } finally {
            activeProcesses.remove(sessionId, process);
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

    private Path buildOutputPath(String sessionId, String correlationId, int segmentIndex) {
        String fileName = "tts_" + sessionId + "_" + correlationId + "_" + segmentIndex + ".wav";
        return Path.of(properties.getOutputDir(), fileName);
    }

    private void publishStarted(String sessionId, String correlationId, String text) {
        eventPublisher.publishEvent(new TtsPlaybackStartedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            sessionId,
            correlationId,
            EventSource.TTS_SERVICE,
            text,
            properties.getVoiceId()
        ));
    }

    private void publishCompleted(String sessionId, String correlationId, String text, long durationMs) {
        eventPublisher.publishEvent(new TtsPlaybackCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            sessionId,
            correlationId,
            EventSource.TTS_SERVICE,
            text,
            durationMs
        ));
    }

    private void publishFailed(String sessionId, String correlationId, String errorCode, String errorMessage) {
        eventPublisher.publishEvent(new TtsPlaybackFailedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            sessionId,
            correlationId,
            EventSource.TTS_SERVICE,
            errorCode,
            errorMessage
        ));
    }

    private Path buildOutputPath(String sessionId, String correlationId) {
        String fileName = "tts_" + sessionId + "_" + correlationId + ".wav";
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
