// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableConfigurationProperties(UiStateRefreshProperties.class)
public class UiStateRefreshConfiguration {

    @Bean
    public TaskScheduler uiStateRefreshTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("tai-ui-state-refresh-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.setAwaitTerminationSeconds(2);
        scheduler.initialize();
        return scheduler;
    }
}
