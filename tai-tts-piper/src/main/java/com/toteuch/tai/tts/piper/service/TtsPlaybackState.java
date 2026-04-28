// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.service;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class TtsPlaybackState {
    private final AtomicReference<String> activeCorrelationId = new AtomicReference<>();

    public String getActiveCorrelationId() {
        return activeCorrelationId.get();
    }

    public void setActiveCorrelationId(String correlationId) {
        activeCorrelationId.set(correlationId);
    }

    public void clearIfActive(String correlationId) {
        activeCorrelationId.compareAndSet(correlationId, null);
    }

    public boolean isActive(String correlationId) {
        return correlationId != null && correlationId.equals(activeCorrelationId.get());
    }
}
