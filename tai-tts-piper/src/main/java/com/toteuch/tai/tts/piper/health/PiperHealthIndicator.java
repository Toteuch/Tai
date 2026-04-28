// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.health;

import com.toteuch.tai.tts.piper.config.TtsPiperProperties;
import com.toteuch.tai.tts.piper.service.PiperSynthesisService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("piper")
public class PiperHealthIndicator implements HealthIndicator {
    private final PiperSynthesisService piperSynthesisService;
    private final TtsPiperProperties properties;

    public PiperHealthIndicator(
            PiperSynthesisService piperSynthesisService, TtsPiperProperties properties) {
        this.piperSynthesisService = piperSynthesisService;
        this.properties = properties;
    }

    @Override
    public Health health() {
        Health.Builder builder =
                piperSynthesisService.isPiperConfigured() ? Health.up() : Health.down();

        return builder.withDetail("executable", properties.getPiper().getExecutable())
                .withDetail("model", properties.getPiper().getModel())
                .withDetail("config", properties.getPiper().getConfig())
                .withDetail("voiceId", properties.getPiper().getVoiceId())
                .build();
    }
}
