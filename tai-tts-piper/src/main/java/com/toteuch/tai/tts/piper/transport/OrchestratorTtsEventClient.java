package com.toteuch.tai.tts.piper.transport;

import com.toteuch.tai.tts.piper.config.TtsPiperProperties;
import com.toteuch.tai.tts.piper.transport.dto.AbstractTransportEventRequest;
import com.toteuch.tai.tts.piper.transport.dto.TransportEventSource;
import com.toteuch.tai.tts.piper.transport.dto.TtsPlaybackCompletedEventRequest;
import com.toteuch.tai.tts.piper.transport.dto.TtsPlaybackFailedEventRequest;
import com.toteuch.tai.tts.piper.transport.dto.TtsPlaybackStartedEventRequest;
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

    public void sendPlaybackStarted(String correlationId, String text, long ms) {
        TtsPlaybackStartedEventRequest request = new TtsPlaybackStartedEventRequest();
        fillCommon(request, correlationId);
        request.setText(text);
        request.setVoiceId(properties.getPiper().getVoiceId());
        request.setSynthesisDurationMs(ms);
        post(properties.getOrchestrator().getCallbacks().getPlaybackStartedPath(), request);
    }

    public void sendPlaybackCompleted(String correlationId, String text, long speechDurationMs) {
        TtsPlaybackCompletedEventRequest request = new TtsPlaybackCompletedEventRequest();
        fillCommon(request, correlationId);
        request.setText(text);
        request.setSpeechDurationMs(speechDurationMs);
        post(properties.getOrchestrator().getCallbacks().getPlaybackCompletedPath(), request);
    }

    public void sendPlaybackFailed(
            String correlationId, String errorCode, String errorMessage, long speechDurationMs) {
        TtsPlaybackFailedEventRequest request = new TtsPlaybackFailedEventRequest();
        fillCommon(request, correlationId);
        request.setErrorCode(errorCode);
        request.setErrorMessage(errorMessage);
        request.setSpeechDurationMs(speechDurationMs);
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
            orchestratorRestClient.post().uri(path).body(request).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send TTS callback | path={}", path, e);
        }
    }
}
