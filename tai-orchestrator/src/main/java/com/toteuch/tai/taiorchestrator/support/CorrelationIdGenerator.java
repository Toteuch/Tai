package com.toteuch.tai.taiorchestrator.support;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CorrelationIdGenerator {
    public String newId() {
        return UUID.randomUUID().toString();
    }
}
