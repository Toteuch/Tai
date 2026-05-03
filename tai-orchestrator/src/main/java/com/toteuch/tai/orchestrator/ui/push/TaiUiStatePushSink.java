// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import java.util.Set;

public interface TaiUiStatePushSink {

    void push(TaiUiState state, Set<UiStateRefreshReason> reasons, Set<String> correlationIds);
}
