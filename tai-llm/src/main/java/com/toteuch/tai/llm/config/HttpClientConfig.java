// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient ollamaRestClient(LlmProperties properties) {
        HttpClient httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofMillis(properties.getOllama().getConnectTimeoutMs()))
                        .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getOllama().getReadTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.getOllama().getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient orchestratorRestClient(LlmProperties properties) {
        HttpClient httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofMillis(
                                        properties.getOrchestrator().getConnectTimeoutMs()))
                        .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(
                Duration.ofMillis(properties.getOrchestrator().getReadTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.getOrchestrator().getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
