package com.toteuch.tai.taiorchestrator.services.stt.whisper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toteuch.tai.taiorchestrator.services.stt.SttClient;
import com.toteuch.tai.taiorchestrator.transport.SttEventController;
import com.toteuch.tai.taiorchestrator.transport.events.TransportEventSource;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttSpeechStartedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTranscriptAcceptedEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTranscriptNoiseEventRequest;
import com.toteuch.tai.taiorchestrator.transport.events.stt.SttTranscriptUnintelligibleEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Primary
public class WhisperSttClient implements SttClient {

    private static final Logger log = LoggerFactory.getLogger(WhisperSttClient.class);

    private final WhisperSttProperties properties;
    private final ObjectMapper objectMapper;
    private final SttEventController eventController;

    public WhisperSttClient(
        WhisperSttProperties properties,
        ObjectMapper objectMapper,
        SttEventController sttEventController
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.eventController = sttEventController;
    }

    @Override
    public void transcribe(Path audioFile) {
        String correlationId = UUID.randomUUID().toString();
        try {
            if (audioFile == null || !Files.exists(audioFile)) {
                log.error("Audio file does not exist: {}", audioFile);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                properties.getPythonExecutable(),
                properties.getScriptPath(),
                audioFile.toString(),
                properties.getModelSize(),
                properties.getDevice(),
                properties.getComputeType()
            );

            processBuilder.redirectErrorStream(true);

            log.debug("Calling Whisper STT | correlationId={} audioFile={} model={} device={} computeType={}",
                correlationId,
                audioFile,
                properties.getModelSize(),
                properties.getDevice(),
                properties.getComputeType());

            Process process = processBuilder.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            int exitCode = process.waitFor();

            log.debug("Whisper STT finished | correlationId={} exitCode={} rawOutput={}",
                correlationId,
                exitCode,
                output);

            if (exitCode != 0) {
                log.error("Whisper STT failed | correlationId={} exitCode={} output={}", correlationId, exitCode, output);
            }

            JsonNode root = objectMapper.readTree(output);

            if (!root.path("success").asBoolean(false)) {
                log.error("Whisper STT failed | correlationId={} output={}", correlationId, output);
            }

            postTranscriptAcceptedEvent(
                correlationId,
                root.path("language").asText(null),
                root.path("language_probability").isNumber() ? root.path("language_probability").asDouble() : null,
                null,
                null,
                null,
                null,
                root.path("text").asText(null)
            );
        } catch (Exception e) {
            log.error("Whisper STT failed | correlationId={} audioFile={}", correlationId, audioFile, e);
        }
    }

    private void postTranscriptAcceptedEvent(
        String correlationId,
        String language,
        Double languageProbability,
        Long durationMs,
        Double averageEnergy,
        String reason,
        Integer suspiciousScore,
        String text
    ) {
        SttTranscriptAcceptedEventRequest response = new SttTranscriptAcceptedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setSource(TransportEventSource.STT_SERVICE);
        response.setCorrelationId(correlationId);
        response.setLanguage(language);
        response.setLanguageProbability(languageProbability);
        response.setDurationMs(durationMs);
        response.setAverageEnergy(averageEnergy);
        response.setReason(reason);
        response.setSuspicionScore(suspiciousScore);
        response.setText(text);
        eventController.onTranscriptAccepted(response);
    }

    private void postTranscriptNoiseEvent(
        String correlationId,
        String language,
        Double languageProbability,
        Long durationMs,
        Double averageEnergy,
        String reason,
        Integer suspiciousScore
    ) {
        SttTranscriptNoiseEventRequest response = new SttTranscriptNoiseEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setSource(TransportEventSource.STT_SERVICE);
        response.setCorrelationId(correlationId);
        response.setLanguage(language);
        response.setLanguageProbability(languageProbability);
        response.setDurationMs(durationMs);
        response.setAverageEnergy(averageEnergy);
        response.setReason(reason);
        response.setSuspicionScore(suspiciousScore);
        eventController.onTranscriptNoise(response);
    }

    private void postTranscriptUnintelligibleEvent(
        String correlationId,
        String language,
        Double languageProbability,
        Long durationMs,
        Double averageEnergy,
        String reason,
        Integer suspiciousScore
    ) {
        SttTranscriptUnintelligibleEventRequest response = new SttTranscriptUnintelligibleEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setSource(TransportEventSource.STT_SERVICE);
        response.setCorrelationId(correlationId);
        response.setLanguage(language);
        response.setLanguageProbability(languageProbability);
        response.setDurationMs(durationMs);
        response.setAverageEnergy(averageEnergy);
        response.setReason(reason);
        response.setSuspicionScore(suspiciousScore);
        eventController.onTranscriptUnintelligible(response);
    }

    private void postSpeechStartedEvent(
        String correlationId,
        String language,
        Double languageProbability,
        Long durationMs,
        Double averageEnergy,
        String reason,
        Integer suspiciousScore
    ) {
        SttSpeechStartedEventRequest response = new SttSpeechStartedEventRequest();
        response.setEventId(UUID.randomUUID().toString());
        response.setCreatedAt(Instant.now());
        response.setSource(TransportEventSource.STT_SERVICE);
        response.setCorrelationId(correlationId);
        response.setLanguage(language);
        response.setLanguageProbability(languageProbability);
        response.setDurationMs(durationMs);
        response.setAverageEnergy(averageEnergy);
        response.setReason(reason);
        response.setSuspicionScore(suspiciousScore);
        eventController.onSpeechStarted(response);
    }
}
