// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ModuleHealthRefreshProperties.class)
public class ModuleHealthRefreshConfiguration {

    @Bean(name = "moduleHealthRefreshExecutor")
    public Executor moduleHealthRefreshExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("tai-module-health-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(32);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(2);
        executor.initialize();
        return executor;
    }
}
