// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import com.toteuch.tai.orchestrator.ui.lifecycle.ApplicationShutdownState;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringUiStateRefreshRequester implements UiStateRefreshRequester {

    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final ApplicationShutdownState shutdownState;

    public SpringUiStateRefreshRequester(
            ApplicationEventPublisher eventPublisher,
            Clock clock,
            ApplicationShutdownState shutdownState) {
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.shutdownState = shutdownState;
    }

    @Override
    public void requestRefresh(UiStateRefreshReason reason) {
        requestRefresh(reason, null);
    }

    @Override
    public void requestRefresh(UiStateRefreshReason reason, String correlationId) {
        // Prevents events from being published during application shutdown
        if (shutdownState.isShuttingDown()) {
            return;
        }
        eventPublisher.publishEvent(
                new UiStateRefreshRequestedEvent(reason, correlationId, clock.instant()));
    }
}
