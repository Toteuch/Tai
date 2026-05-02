// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.lifecycle;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationShutdownState {

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        shuttingDown.set(true);
    }

    public boolean isShuttingDown() {
        return shuttingDown.get();
    }
}
