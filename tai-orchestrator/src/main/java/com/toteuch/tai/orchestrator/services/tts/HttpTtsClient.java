package com.toteuch.tai.orchestrator.services.tts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpTtsClient implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(HttpTtsClient.class);

    private final RestClient restClient;

    public HttpTtsClient(@Value("${tai.tts.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void speak(String correlationId, String text) {
        try {
            restClient
                    .post()
                    .uri("/tts/speak")
                    .body(new TtsSpeakRequest(correlationId, text))
                    .retrieve()
                    .toBodilessEntity();

            log.info("TTS speak request sent | correlationId={}", correlationId);
        } catch (Exception e) {
            log.error("Failed to call TTS speak endpoint | correlationId={}", correlationId, e);
        }
    }

    @Override
    public void stop(String correlationId) {
        try {
            restClient
                    .post()
                    .uri("/tts/stop")
                    .body(new TtsStopRequest(correlationId))
                    .retrieve()
                    .toBodilessEntity();

            log.info("TTS stop request sent | correlationId={}", correlationId);
        } catch (Exception e) {
            log.error("Failed to call TTS stop endpoint | correlationId={}", correlationId, e);
        }
    }

    private record TtsSpeakRequest(String correlationId, String text) {}

    private record TtsStopRequest(String correlationId) {}
}
