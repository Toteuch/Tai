package com.toteuch.tai.taiorchestrator.services.tts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockTtsClient implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(MockTtsClient.class);

    public MockTtsClient() {
        log.debug("MockTtsClient initialized");
    }

    @Override
    public void speak(String correlationId, String text) {
        log.debug("Mock TTS speak | correlationId={} text={}", correlationId, text);
    }

    @Override
    public void stop(String correlationId) {
        log.debug("Mock TTS stop | correlationId={}", correlationId);
    }
}
