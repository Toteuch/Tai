// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.lifecycle.ApplicationShutdownState;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshReason;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiStateRefreshModuleHealthRefreshListener implements ModuleHealthRefreshListener {
    private static final Logger traceLog = LoggerFactory.getLogger("tai.trace");

    private final UiStateRefreshRequester uiStateRefreshRequester;
    private final ApplicationShutdownState shutdownState;

    public UiStateRefreshModuleHealthRefreshListener(
            UiStateRefreshRequester uiStateRefreshRequester,
            ApplicationShutdownState shutdownState) {
        this.uiStateRefreshRequester = uiStateRefreshRequester;
        this.shutdownState = shutdownState;
    }

    @Override
    public void onModuleHealthRefreshed(TaiModule module, RefreshReason reason) {
        if (shutdownState.isShuttingDown()) {
            return;
        }
        uiStateRefreshRequester.requestRefresh(UiStateRefreshReason.HEALTH_REFRESH);
    }
}
