// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm;

import com.toteuch.tai.llm.config.LlmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(LlmProperties.class)
public class TaiLlmApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaiLlmApplication.class, args);
    }
}
