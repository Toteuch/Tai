package com.toteuch.tai.llm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI taiLlmOpenApi() {
        return new OpenAPI().info(new Info().title("Tai LLM Service API").version("0.1.0").description("Asynchronous LLM microservice backed by Ollama."));
    }
}
