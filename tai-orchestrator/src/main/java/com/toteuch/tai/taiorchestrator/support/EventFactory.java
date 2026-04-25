package com.toteuch.tai.taiorchestrator.support;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventFactory {

    public String newEventId() {
        return UUID.randomUUID().toString();
    }

    public Instant now() {
        return Instant.now();
    }
}
