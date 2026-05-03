// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(TaiUiStatePushSink.class)
public class NoOpTaiUiStatePushSink implements TaiUiStatePushSink {

    @Override
    public void push(
            TaiUiState state, Set<UiStateRefreshReason> reasons, Set<String> correlationIds) {
        // No-op until SSE live push is implemented.
    }
}
