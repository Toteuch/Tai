// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.transport;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.tts.TtsPlaybackCompletedEvent;
import com.toteuch.tai.events.tts.TtsPlaybackFailedEvent;
import com.toteuch.tai.events.tts.TtsPlaybackStartedEvent;
import com.toteuch.tai.tts.piper.config.TtsPiperProperties;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrchestratorTtsEventClient {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorTtsEventClient.class);

    private final RestClient orchestratorRestClient;
    private final TtsPiperProperties properties;

    public OrchestratorTtsEventClient(
            RestClient orchestratorRestClient, TtsPiperProperties properties) {
        this.orchestratorRestClient = orchestratorRestClient;
        this.properties = properties;
    }

    public void sendPlaybackStarted(String correlationId, String text, long synthesisDurationMs) {
        TtsPlaybackStartedEvent event =
                new TtsPlaybackStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        text,
                        synthesisDurationMs);

        post(properties.getOrchestrator().getCallbacks().getPlaybackStartedPath(), event);
    }

    public void sendPlaybackCompleted(String correlationId, String text, long speechDurationMs) {
        TtsPlaybackCompletedEvent event =
                new TtsPlaybackCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        text,
                        speechDurationMs);

        post(properties.getOrchestrator().getCallbacks().getPlaybackCompletedPath(), event);
    }

    public void sendPlaybackFailed(
            String correlationId, String errorCode, String errorMessage, long speechDurationMs) {
        TtsPlaybackFailedEvent event =
                new TtsPlaybackFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.TTS_SERVICE,
                        errorCode,
                        errorMessage,
                        speechDurationMs);

        post(properties.getOrchestrator().getCallbacks().getPlaybackFailedPath(), event);
    }

    private void post(String path, Object request) {
        try {
            orchestratorRestClient.post().uri(path).body(request).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send TTS callback | path={}", path, e);
        }
    }
}
