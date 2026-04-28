package com.toteuch.tai.orchestrator.system.health;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SystemHealthProperties.class)
public class SystemHealthConfiguration {}
