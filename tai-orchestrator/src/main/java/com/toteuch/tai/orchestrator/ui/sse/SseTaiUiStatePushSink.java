// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.sse;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import com.toteuch.tai.orchestrator.ui.push.TaiUiStatePushSink;
import com.toteuch.tai.orchestrator.ui.push.UiStateRefreshReason;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SseTaiUiStatePushSink implements TaiUiStatePushSink {

    private final TaiUiSseEmitterRegistry emitterRegistry;

    public SseTaiUiStatePushSink(TaiUiSseEmitterRegistry emitterRegistry) {
        this.emitterRegistry = emitterRegistry;
    }

    @Override
    public void push(
            TaiUiState state, Set<UiStateRefreshReason> reasons, Set<String> correlationIds) {
        Set<String> reasonNames =
                reasons.stream()
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());

        emitterRegistry.broadcast(state, reasonNames, correlationIds);
    }
}
