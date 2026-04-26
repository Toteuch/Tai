package com.toteuch.tai.tts.piper.service;

import com.toteuch.tai.tts.piper.transport.OrchestratorTtsEventClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

@Service
public class TtsPlaybackService {
    private static final Logger log = LoggerFactory.getLogger(TtsPlaybackService.class);

    private final PiperSynthesisService piperSynthesisService;
    private final JavaAudioPlaybackService playbackService;
    private final OrchestratorTtsEventClient eventClient;
    private final TtsPlaybackState state;

    public TtsPlaybackService(
        PiperSynthesisService piperSynthesisService,
        JavaAudioPlaybackService playbackService,
        OrchestratorTtsEventClient eventClient,
        TtsPlaybackState state
    ) {
        this.piperSynthesisService = piperSynthesisService;
        this.playbackService = playbackService;
        this.eventClient = eventClient;
        this.state = state;
    }

    @Async("ttsTaskExecutor")
    public void speakAsync(String correlationId, String text) {
        log.info("TTS speech requested | correlationId={}", correlationId);
        state.setActiveCorrelationId(correlationId);
        Path wavFile = piperSynthesisService.synthesize(correlationId, text);
        try {
            if (!state.isActive(correlationId)) {
                log.info("TTS speech superseded before playback | correlationId={}", correlationId);
                return;
            }

            eventClient.sendPlaybackStarted(correlationId, text);

            Instant playbackStartedAt = Instant.now();
            playbackService.playBlocking(wavFile);
            long speechDurationMs = Duration.between(playbackStartedAt, Instant.now()).toMillis();

            if (!state.isActive(correlationId)) {
                log.info("TTS speech stopped before completion callback | correlationId={}", correlationId);
                return;
            }

            eventClient.sendPlaybackCompleted(correlationId, text, speechDurationMs);
        } catch (Exception e) {
            log.warn("TTS playback failed | correlationId={}", correlationId, e);
            eventClient.sendPlaybackFailed(correlationId, "PIPER_TTS_ERROR", e.getMessage());
        } finally {
            state.clearIfActive(correlationId);
            deleteGeneratedWav(correlationId, wavFile);
        }
    }

    public void stop(String correlationId) {
        if (!state.isActive(correlationId)) {
            log.info("Ignoring TTS stop for non-active playback | correlationId={} activeCorrelationId={}",
                correlationId,
                state.getActiveCorrelationId()
            );
            return;
        }

        log.info("Stopping TTS playback | correlationId={}", correlationId);
        playbackService.stop();
        state.clearIfActive(correlationId);
    }

    private void deleteGeneratedWav(String correlationId, Path wavFile) {
        if (wavFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(wavFile);
            log.debug("Deleted generated WAV | correlationId={} file={}", correlationId, wavFile.toAbsolutePath());
        } catch (Exception e) {
            log.warn("Failed to delete generated WAV | correlationId={} file={}",
                correlationId,
                wavFile.toAbsolutePath(),
                e
            );
        }
    }
}
