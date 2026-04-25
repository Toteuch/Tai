package com.toteuch.tai.tts.piper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfig {
    @Bean
    public RestClient orchestratorRestClient(TtsPiperProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getOrchestrator().getConnectTimeoutMs()))
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getOrchestrator().getReadTimeoutMs()));

        return RestClient.builder()
            .baseUrl(properties.getOrchestrator().getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }
}
