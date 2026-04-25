package com.toteuch.tai.tts.piper.transport;

import com.toteuch.tai.tts.piper.config.TtsPiperProperties;
import com.toteuch.tai.tts.piper.transport.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrchestratorTtsEventClient {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorTtsEventClient.class);

    private final RestClient orchestratorRestClient;
    private final TtsPiperProperties properties;

    public OrchestratorTtsEventClient(RestClient orchestratorRestClient, TtsPiperProperties properties) {
        this.orchestratorRestClient = orchestratorRestClient;
        this.properties = properties;
    }

    public void sendPlaybackStarted(String correlationId, String text) {
        TtsPlaybackStartedEventRequest request = new TtsPlaybackStartedEventRequest();
        fillCommon(request, correlationId);
        request.setText(text);
        request.setVoiceId(properties.getPiper().getVoiceId());
        post(properties.getOrchestrator().getCallbacks().getPlaybackStartedPath(), request);
    }

    public void sendPlaybackCompleted(String correlationId, String text, long speechDurationMs) {
        TtsPlaybackCompletedEventRequest request = new TtsPlaybackCompletedEventRequest();
        fillCommon(request, correlationId);
        request.setText(text);
        request.setSpeechDurationMs(speechDurationMs);
        post(properties.getOrchestrator().getCallbacks().getPlaybackCompletedPath(), request);
    }

    public void sendPlaybackFailed(String correlationId, String errorCode, String errorMessage) {
        TtsPlaybackFailedEventRequest request = new TtsPlaybackFailedEventRequest();
        fillCommon(request, correlationId);
        request.setErrorCode(errorCode);
        request.setErrorMessage(errorMessage);
        post(properties.getOrchestrator().getCallbacks().getPlaybackFailedPath(), request);
    }

    private void fillCommon(AbstractTransportEventRequest request, String correlationId) {
        request.setEventId(UUID.randomUUID().toString());
        request.setCreatedAt(Instant.now());
        request.setSource(TransportEventSource.TTS_SERVICE);
        request.setCorrelationId(correlationId);
    }

    private void post(String path, Object request) {
        try {
            orchestratorRestClient.post()
                .uri(path)
                .body(request)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send TTS callback | path={}", path, e);
        }
    }
}
