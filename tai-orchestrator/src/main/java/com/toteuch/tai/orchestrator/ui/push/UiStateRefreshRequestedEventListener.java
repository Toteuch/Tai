// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import com.toteuch.tai.orchestrator.ui.lifecycle.ApplicationShutdownState;
import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import com.toteuch.tai.orchestrator.ui.projection.TaiUiStateProjectionService;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "tai.ui.state-refresh",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class UiStateRefreshRequestedEventListener {
    private static final Logger traceLog =
            LoggerFactory.getLogger(UiStateRefreshRequestedEventListener.class);

    private final UiStateRefreshProperties properties;
    private final TaiUiStateProjectionService projectionService;
    private final TaiUiStatePushSink pushSink;
    private final TaskScheduler taskScheduler;
    private final Clock clock;
    private final ApplicationShutdownState shutdownState;

    private final Object monitor = new Object();
    private final EnumSet<UiStateRefreshReason> pendingReasons =
            EnumSet.noneOf(UiStateRefreshReason.class);
    private final LinkedHashSet<String> pendingCorrelationIds = new LinkedHashSet<>();

    private ScheduledFuture<?> scheduledRefresh;

    public UiStateRefreshRequestedEventListener(
            UiStateRefreshProperties properties,
            TaiUiStateProjectionService projectionService,
            TaiUiStatePushSink pushSink,
            @Qualifier("uiStateRefreshTaskScheduler") TaskScheduler taskScheduler,
            Clock clock,
            ApplicationShutdownState applicationShutdownState) {
        this.properties = properties;
        this.projectionService = projectionService;
        this.pushSink = pushSink;
        this.taskScheduler = taskScheduler;
        this.clock = clock;
        this.shutdownState = applicationShutdownState;
    }

    @EventListener
    public void onUiStateRefreshRequested(UiStateRefreshRequestedEvent event) {
        // Prevents events from being published during application shutdown
        if (shutdownState.isShuttingDown()) {
            // To not enter in the synchronized block
            return;
        }

        synchronized (monitor) {
            if (shutdownState.isShuttingDown()) {
                // If the application state changed while the thread was waiting for the lock
                return;
            }
            pendingReasons.add(event.reason());

            if (event.correlationId() != null && !event.correlationId().isBlank()) {
                pendingCorrelationIds.add(event.correlationId());
            }

            if (scheduledRefresh != null && !scheduledRefresh.isDone()) {
                return;
            }

            Instant refreshAt = clock.instant().plus(properties.getDebounce());

            scheduledRefresh = taskScheduler.schedule(this::flush, refreshAt);
        }
    }

    private void flush() {
        Set<UiStateRefreshReason> reasons;
        Set<String> correlationIds;

        synchronized (monitor) {
            reasons = EnumSet.copyOf(pendingReasons);
            correlationIds = Set.copyOf(pendingCorrelationIds);

            pendingReasons.clear();
            pendingCorrelationIds.clear();
            scheduledRefresh = null;
        }

        TaiUiState state = projectionService.rebuild();
        pushSink.push(state, reasons, correlationIds);
    }
}
