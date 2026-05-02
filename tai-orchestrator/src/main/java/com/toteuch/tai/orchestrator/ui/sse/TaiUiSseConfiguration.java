// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.sse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaiUiSseProperties.class)
public class TaiUiSseConfiguration {}
