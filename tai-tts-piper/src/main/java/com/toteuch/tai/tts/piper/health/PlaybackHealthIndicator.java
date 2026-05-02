package com.toteuch.tai.tts.piper.health;

import com.toteuch.tai.tts.piper.service.TtsPlaybackState;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("playback")
public class PlaybackHealthIndicator implements HealthIndicator {
    private final TtsPlaybackState state;

    public PlaybackHealthIndicator(TtsPlaybackState state) {
        this.state = state;
    }

    @Override
    public Health health() {
        if (state.getActiveCorrelationId() != null) {
            return Health.up()
                    .withDetail("activeCorrelationId", state.getActiveCorrelationId())
                    .build();
        }
        return Health.up().build();
    }
}
