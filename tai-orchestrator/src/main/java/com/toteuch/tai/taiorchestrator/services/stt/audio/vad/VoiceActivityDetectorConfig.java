package com.toteuch.tai.taiorchestrator.services.stt.audio.vad;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VoiceActivityDetectorProperties.class)
public class VoiceActivityDetectorConfig {
}
