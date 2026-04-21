package com.toteuch.tai.taiorchestrator.services.tts;

import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackCompletedEvent;
import com.toteuch.tai.taiorchestrator.events.inbound.TtsPlaybackStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@Primary
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
        this.playbackDelayMs = playbackDelayMs;
        this.voiceId = voiceId;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void speak(String sessionId, String correlationId, String text) {
        log.info("SlowMockTtsClient speak requested | sessionId={} correlationId={} delayMs={}",
            sessionId, correlationId, playbackDelayMs);

        stop(sessionId);

        Future<?> future = executorService.submit(() -> {
            try {
                publishStarted(sessionId, correlationId, text);

                Thread.sleep(playbackDelayMs);

                publishCompleted(sessionId, correlationId, text, playbackDelayMs);

                log.info("SlowMockTtsClient playback completed | sessionId={} correlationId={}",
                    sessionId, correlationId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("SlowMockTtsClient playback interrupted | sessionId={} correlationId={}",
                    sessionId, correlationId);
            } finally {
                activeSessions.remove(sessionId);
            }
        });

        activeSessions.put(sessionId, future);
    }

    @Override
    public void stop(String sessionId) {
        Future<?> future = activeSessions.remove(sessionId);
        if (future != null) {
            boolean cancelled = future.cancel(true);
            log.info("SlowMockTtsClient stop requested | sessionId={} cancelled={}", sessionId, cancelled);
        }
    }

    private void publishStarted(String sessionId, String correlationId, String text) {
        TtsPlaybackStartedEvent event = new TtsPlaybackStartedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            sessionId,
            correlationId,
            EventSource.TTS_SERVICE,
            text,
            voiceId
        );

        eventPublisher.publishEvent(event);
    }

    private void publishCompleted(String sessionId, String correlationId, String text, long durationMs) {
        TtsPlaybackCompletedEvent event = new TtsPlaybackCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            sessionId,
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
