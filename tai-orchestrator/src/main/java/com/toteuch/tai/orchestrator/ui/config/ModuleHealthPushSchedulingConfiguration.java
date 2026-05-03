// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ModuleHealthPushSchedulingConfiguration {

    @Bean
    public TaskScheduler moduleHealthUiPushTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("tai-ui-health-push-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.setAwaitTerminationSeconds(2);
        scheduler.initialize();
        return scheduler;
    }
}
