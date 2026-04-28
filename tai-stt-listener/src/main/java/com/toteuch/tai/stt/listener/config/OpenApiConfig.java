// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI taiSttListenerOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Tai STT Listener API")
                                .version("0.1.0")
                                .description(
                                        "Java microphone capture microservice for Tai STT flows."));
    }
}
