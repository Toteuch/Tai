// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class TaiUiStateStore {

    private final AtomicReference<TaiUiState> current = new AtomicReference<>();

    public Optional<TaiUiState> current() {
        return Optional.ofNullable(current.get());
    }

    public TaiUiState update(TaiUiState state) {
        current.set(state);
        return state;
    }
}
