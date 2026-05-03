// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.stt.SttSpeechStartedEvent;
import com.toteuch.tai.events.stt.SttTranscriptAcceptedEvent;
import com.toteuch.tai.events.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.events.stt.SttTranscriptUnintelligibleEvent;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;
import com.toteuch.tai.stt.listener.gatekeeper.RejectionCategory;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorSttEventClient {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorSttEventClient.class);

    private final SttListenerProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OrchestratorSttEventClient(SttListenerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(
                                Duration.ofMillis(
                                        properties.getOrchestrator().getConnectTimeoutMs()))
                        .build();
    }

    public void sendCallback(
            String correlationId,
            SpeechSegment segment,
            TranscriptionResult transcription,
            GatekeeperDecision decision) {
        if (decision.accepted()) {
            sendAccepted(correlationId, segment, transcription, decision);
            return;
        }

        if (decision.rejectionCategory() == RejectionCategory.UNINTELLIGIBLE) {
            sendUnintelligible(
                    properties.getOrchestrator().getCallbacks().getTranscriptUnintelligiblePath(),
                    correlationId,
                    segment,
                    transcription,
                    decision);
            return;
        }

        sendNoise(
                properties.getOrchestrator().getCallbacks().getTranscriptNoisePath(),
                correlationId,
                segment,
                transcription,
                decision);
    }

    public void sendSpeechStarted(String correlationId, double averageEnergy, double peakEnergy) {
        SttSpeechStartedEvent event =
                new SttSpeechStartedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        averageEnergy,
                        peakEnergy);

        post(
                properties.getOrchestrator().getCallbacks().getSpeechStartedPath(),
                event,
                correlationId);
    }

    private void sendAccepted(
            String correlationId,
            SpeechSegment segment,
            TranscriptionResult transcription,
            GatekeeperDecision decision) {
        SttTranscriptAcceptedEvent event =
                new SttTranscriptAcceptedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        transcription.text(),
                        transcription.language(),
                        transcription.languageProbability(),
                        segment.durationMs(),
                        transcription.transcriptionDurationMs());

        post(
                properties.getOrchestrator().getCallbacks().getTranscriptAcceptedPath(),
                event,
                correlationId);
    }

    private void sendNoise(
            String path,
            String correlationId,
            SpeechSegment segment,
            TranscriptionResult transcription,
            GatekeeperDecision decision) {
        SttTranscriptNoiseEvent event =
                new SttTranscriptNoiseEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        segment.averageEnergy(),
                        decision.reason(),
                        decision.suspicionScore(),
                        segment.durationMs(),
                        transcription != null ? transcription.transcriptionDurationMs() : null);

        post(path, event, correlationId);
    }

    private void sendUnintelligible(
            String path,
            String correlationId,
            SpeechSegment segment,
            TranscriptionResult transcription,
            GatekeeperDecision decision) {
        SttTranscriptUnintelligibleEvent event =
                new SttTranscriptUnintelligibleEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        correlationId,
                        EventSource.STT_SERVICE,
                        transcription != null ? transcription.language() : null,
                        transcription != null ? transcription.languageProbability() : null,
                        segment.averageEnergy(),
                        decision.reason(),
                        decision.suspicionScore(),
                        segment.durationMs(),
                        transcription != null ? transcription.transcriptionDurationMs() : null);

        post(path, event, correlationId);
    }

    private void post(String path, Object body, String correlationId) {
        try {
            String json = objectMapper.writeValueAsString(body);

            URI uri = URI.create(properties.getOrchestrator().getBaseUrl() + path);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(uri)
                            .version(HttpClient.Version.HTTP_1_1)
                            .timeout(
                                    Duration.ofMillis(
                                            properties.getOrchestrator().getReadTimeoutMs()))
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "STT callback failed | correlationId={} path={} status={} body={}",
                        correlationId,
                        path,
                        response.statusCode(),
                        response.body());
            }
        } catch (Exception e) {
            log.warn(
                    "Failed to send STT callback | correlationId={} path={}",
                    correlationId,
                    path,
                    e);
        }
    }
}
