// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

public interface UiStateRefreshRequester {

    void requestRefresh(UiStateRefreshReason reason);

    void requestRefresh(UiStateRefreshReason reason, String correlationId);
}
