package com.toteuch.tai.taiorchestrator.services.tts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockTtsClient implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(MockTtsClient.class);

    public MockTtsClient() {
        log.info("MockTtsClient initialized");
    }

    @Override
    public void speak(String sessionId, String correlationId, String text) {
        log.info("Mock TTS speak | sessionId={} correlationId={} text={}", sessionId, correlationId, text);
    }

    @Override
    public void stop(String sessionId) {
        log.info("Mock TTS stop | sessionId={}", sessionId);
    }
}
