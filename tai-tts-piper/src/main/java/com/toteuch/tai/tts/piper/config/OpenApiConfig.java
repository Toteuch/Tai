package com.toteuch.tai.tts.piper.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI taiTtsPiperOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Tai TTS Piper Service API")
                .version("0.1.0")
                .description("Asynchronous Piper TTS microservice."));
    }
}
