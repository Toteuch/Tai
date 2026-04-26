package com.toteuch.tai.stt.listener.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;
import com.toteuch.tai.stt.listener.gatekeeper.RejectionCategory;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;
import com.toteuch.tai.stt.listener.transport.dto.AbstractTransportEventRequest;
import com.toteuch.tai.stt.listener.transport.dto.SttTranscriptAcceptedEventRequest;
import com.toteuch.tai.stt.listener.transport.dto.SttTranscriptRejectedEventRequest;
import com.toteuch.tai.stt.listener.transport.dto.TransportEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class OrchestratorSttEventClient {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorSttEventClient.class);

    private final SttListenerProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OrchestratorSttEventClient(
        SttListenerProperties properties,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofMillis(properties.getOrchestrator().getConnectTimeoutMs()))
            .build();
    }

    public void sendCallback(
        String correlationId,
        SpeechSegment segment,
        TranscriptionResult transcription,
        GatekeeperDecision decision
    ) {
        if (decision.accepted()) {
            sendAccepted(correlationId, segment, transcription, decision);
            return;
        }

        if (decision.rejectionCategory() == RejectionCategory.UNINTELLIGIBLE) {
            sendRejected(
                properties.getOrchestrator().getCallbacks().getTranscriptUnintelligiblePath(),
                correlationId,
                segment,
                transcription,
                decision
            );
            return;
        }

        sendRejected(
            properties.getOrchestrator().getCallbacks().getTranscriptNoisePath(),
            correlationId,
            segment,
            transcription,
            decision
        );
    }

    private void sendAccepted(
        String correlationId,
        SpeechSegment segment,
        TranscriptionResult transcription,
        GatekeeperDecision decision
    ) {
        SttTranscriptAcceptedEventRequest request = new SttTranscriptAcceptedEventRequest();
        fillCommon(request, correlationId);
        request.setText(transcription.text());
        request.setLanguage(transcription.language());
        request.setLanguageProbability(transcription.languageProbability());
        request.setDurationMs(segment.durationMs());
        request.setAverageEnergy(segment.averageEnergy());
        request.setReason(decision.reason());
        request.setSuspicionScore(decision.suspicionScore());

        post(
            properties.getOrchestrator().getCallbacks().getTranscriptAcceptedPath(),
            request,
            correlationId
        );
    }

    private void sendRejected(
        String path,
        String correlationId,
        SpeechSegment segment,
        TranscriptionResult transcription,
        GatekeeperDecision decision
    ) {
        SttTranscriptRejectedEventRequest request = new SttTranscriptRejectedEventRequest();
        fillCommon(request, correlationId);

        if (transcription != null) {
            request.setLanguage(transcription.language());
            request.setLanguageProbability(transcription.languageProbability());
        }

        request.setDurationMs(segment.durationMs());
        request.setAverageEnergy(segment.averageEnergy());
        request.setReason(decision.reason());
        request.setSuspicionScore(decision.suspicionScore());

        post(path, request, correlationId);
    }

    private void fillCommon(AbstractTransportEventRequest request, String correlationId) {
        request.setEventId(UUID.randomUUID().toString());
        request.setCreatedAt(Instant.now());
        request.setSource(TransportEventSource.STT_SERVICE);
        request.setCorrelationId(correlationId);
    }

    private void post(String path, Object body, String correlationId) {
        try {
            String json = objectMapper.writeValueAsString(body);

            URI uri = URI.create(properties.getOrchestrator().getBaseUrl() + path);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(properties.getOrchestrator().getReadTimeoutMs()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                    "STT callback failed | correlationId={} path={} status={} body={}",
                    correlationId,
                    path,
                    response.statusCode(),
                    response.body()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send STT callback | correlationId={} path={}", correlationId, path, e);
        }
    }
}
