package com.toteuch.tai.llm.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean(name = "llmTaskExecutor")
    public Executor llmTaskExecutor(LlmProperties p) {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(p.getAsync().getCorePoolSize());
        e.setMaxPoolSize(p.getAsync().getMaxPoolSize());
        e.setQueueCapacity(p.getAsync().getQueueCapacity());
        e.setThreadNamePrefix("tai-llm-");
        e.initialize();
        return e;
    }
}
