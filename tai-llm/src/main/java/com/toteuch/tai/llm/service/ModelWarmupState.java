package com.toteuch.tai.llm.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.*;

@Component
public class ModelWarmupState {
    private final AtomicBoolean warm = new AtomicBoolean(false);
    private final AtomicReference<Instant> lastWarmupAt = new AtomicReference<>();
    private final AtomicReference<String> lastError = new AtomicReference<>();

    public boolean isWarm() {
        return warm.get();
    }

    public Instant getLastWarmupAt() {
        return lastWarmupAt.get();
    }

    public String getLastError() {
        return lastError.get();
    }

    public void markWarm() {
        warm.set(true);
        lastWarmupAt.set(Instant.now());
        lastError.set(null);
    }

    public void markFailed(String error) {
        warm.set(false);
        lastError.set(error);
    }
}
