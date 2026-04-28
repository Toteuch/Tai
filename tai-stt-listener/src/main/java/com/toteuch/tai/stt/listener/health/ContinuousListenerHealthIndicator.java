// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.health;

import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.listener.ContinuousListeningService;
import com.toteuch.tai.stt.listener.listener.ListeningRuntimeStatus;
import com.toteuch.tai.stt.listener.listener.ListeningState;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineSummary;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

@Component("continuousListener")
public class ContinuousListenerHealthIndicator implements HealthIndicator {
    private final ContinuousListeningService listeningService;
    private final SttListenerProperties properties;

    public ContinuousListenerHealthIndicator(
            ContinuousListeningService listeningService, SttListenerProperties properties) {
        this.listeningService = listeningService;
        this.properties = properties;
    }

    @Override
    public Health health() {
        ListeningRuntimeStatus status = listeningService.status();

        Health.Builder builder = healthBuilder(status);

        builder.withDetail("running", status.running());
        builder.withDetail("state", status.state().name());
        builder.withDetail("activeCorrelationId", safe(status.activeCorrelationId()));
        builder.withDetail("lastSegmentAt", safe(status.lastSegmentAt()));
        builder.withDetail("lastError", safe(status.lastError()));
        builder.withDetail("autoStart", properties.getListener().isAutoStart());
        builder.withDetail(
                "publishFinalCallbacks", properties.getListener().isPublishFinalCallbacks());
        builder.withDetail(
                "publishSpeechStartedCallbacks",
                properties.getListener().isPublishSpeechStartedCallbacks());

        SttPipelineSummary lastResult = status.lastResult();

        if (lastResult != null) {
            builder.withDetail("lastAccepted", lastResult.accepted());
            builder.withDetail("lastDecision", lastResult.reason());
            builder.withDetail("lastRejectionCategory", lastResult.rejectionCategory());
            builder.withDetail("lastTranscript", lastResult.text());
            builder.withDetail("lastLanguage", lastResult.language());
            builder.withDetail("lastCompletedAt", lastResult.completedAt());
        }

        return builder.build();
    }

    private Object safe(Object o) {
        if (o == null) {
            return "";
        }
        return o;
    }

    private Health.Builder healthBuilder(ListeningRuntimeStatus status) {
        if (status.state() == ListeningState.ERROR) {
            return Health.down();
        }

        if (status.state() == ListeningState.STOPPED && properties.getListener().isAutoStart()) {
            return Health.status(Status.OUT_OF_SERVICE);
        }

        return Health.up();
    }
}
