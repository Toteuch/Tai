package com.toteuch.tai.taiorchestrator.support;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaiLoggingProperties.class)
public class LoggingConfig {
}
