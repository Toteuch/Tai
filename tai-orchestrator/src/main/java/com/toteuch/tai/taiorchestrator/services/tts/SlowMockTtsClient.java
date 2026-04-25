package com.toteuch.tai.taiorchestrator.services.tts;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.tts.TtsPlaybackStartedEvent;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class SlowMockTtsClient implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(SlowMockTtsClient.class);

    private final long playbackDelayMs;
    private final String voiceId;
    private final ApplicationEventPublisher eventPublisher;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentMap<String, Future<?>> activeSessions = new ConcurrentHashMap<>();

    public SlowMockTtsClient(
        @Value("${tai.mock.tts.playback-delay-ms:6000}") long playbackDelayMs,
        @Value("${tai.mock.tts.voice-id:slow-mock-voice}") String voiceId,
        ApplicationEventPublisher eventPublisher
    ) {
        log.debug("SlowMockTtsClient initialized");
        this.playbackDelayMs = playbackDelayMs;
        this.voiceId = voiceId;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void speak(String correlationId, String text) {
        log.debug("SlowMockTtsClient speak requested | correlationId={} delayMs={}",
            correlationId, playbackDelayMs);

        stop(correlationId);

        Future<?> future = executorService.submit(() -> {
            try {
                publishStarted(correlationId, text);

                Thread.sleep(playbackDelayMs);

                publishCompleted(correlationId, text, playbackDelayMs);

                log.debug("SlowMockTtsClient playback completed | correlationId={}",
                    correlationId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("SlowMockTtsClient playback interrupted | correlationId={}",
                    correlationId);
            } finally {
                activeSessions.remove(correlationId);
            }
        });

        activeSessions.put(correlationId, future);
    }

    @Override
    public void stop(String correlationId) {
        Future<?> future = activeSessions.remove(correlationId);
        if (future != null) {
            boolean cancelled = future.cancel(true);
            log.debug("SlowMockTtsClient stop requested | correlationId={} cancelled={}", correlationId, cancelled);
        }
    }

    private void publishStarted(String correlationId, String text) {
        TtsPlaybackStartedEvent event = new TtsPlaybackStartedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            correlationId,
            EventSource.TTS_SERVICE,
            text,
            voiceId
        );

        eventPublisher.publishEvent(event);
    }

    private void publishCompleted(String correlationId, String text, long durationMs) {
        TtsPlaybackCompletedEvent event = new TtsPlaybackCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            correlationId,
            EventSource.TTS_SERVICE,
            text,
            durationMs
        );

        eventPublisher.publishEvent(event);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
