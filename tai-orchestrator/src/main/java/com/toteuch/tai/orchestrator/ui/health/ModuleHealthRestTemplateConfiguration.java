// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import java.time.Duration;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ModuleHealthRestTemplateConfiguration {

    @Bean
    public RestTemplate moduleHealthRestTemplate(
            RestTemplateBuilder restTemplateBuilder, ModuleHealthRefreshProperties properties) {
        return restTemplateBuilder
                .requestFactory(() -> requestFactory(properties.getRequestTimeout()))
                .errorHandler(
                        new ResponseErrorHandler() {
                            @Override
                            public boolean hasError(ClientHttpResponse response) {
                                return false;
                            }
                        })
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory(Duration timeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }
}
