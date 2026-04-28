// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.transcription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpWhisperTranscriptionClient implements WhisperTranscriptionClient {
    private static final Logger log = LoggerFactory.getLogger(HttpWhisperTranscriptionClient.class);

    private final SttListenerProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public HttpWhisperTranscriptionClient(
            SttListenerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofMillis(properties.getWhisper().getConnectTimeoutMs()))
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();
    }

    @Override
    public TranscriptionResult transcribe(String correlationId, Path audioFile) {
        if (audioFile == null || !Files.exists(audioFile)) {
            return TranscriptionResult.failure(
                    "AUDIO_FILE_NOT_FOUND", "Audio file does not exist: " + audioFile);
        }

        try {
            byte[] audioBytes = Files.readAllBytes(audioFile);

            log.info(
                    "Calling Whisper raw transcription | correlationId={} file={} size={}",
                    correlationId,
                    audioFile.toAbsolutePath(),
                    audioBytes.length);

            if (audioBytes.length == 0) {
                return TranscriptionResult.failure(
                        "AUDIO_FILE_EMPTY", "Audio file is empty: " + audioFile.toAbsolutePath());
            }

            URI uri =
                    URI.create(
                            properties.getWhisper().getBaseUrl()
                                    + properties.getWhisper().getTranscribeRawPath());

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(uri)
                            .version(HttpClient.Version.HTTP_1_1)
                            .timeout(Duration.ofMillis(properties.getWhisper().getReadTimeoutMs()))
                            .header("Accept", "application/json")
                            .header("Content-Type", "audio/wav")
                            .header("X-Correlation-Id", correlationId)
                            .header("X-Filename", audioFile.getFileName().toString())
                            .POST(HttpRequest.BodyPublishers.ofByteArray(audioBytes))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return TranscriptionResult.failure(
                        "WHISPER_HTTP_ERROR",
                        "Whisper service returned status="
                                + response.statusCode()
                                + " body="
                                + response.body());
            }

            return objectMapper.readValue(response.body(), TranscriptionResult.class);
        } catch (Exception e) {
            log.warn(
                    "Whisper transcription call failed | correlationId={} file={}",
                    correlationId,
                    audioFile,
                    e);

            return TranscriptionResult.failure("WHISPER_HTTP_ERROR", e.getMessage());
        }
    }
}
