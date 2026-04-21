package com.toteuch.tai.taiorchestrator.services.llm.ollama;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaConfig {

    @Bean
    public RestTemplate ollamaRestTemplate(
        RestTemplateBuilder builder,
        OllamaProperties properties
    ) {
        return builder
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
            .readTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
            .build();
    }
}
