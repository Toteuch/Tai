package com.toteuch.tai.taiorchestrator.session;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemorySessionStore implements SessionStore {

    private final ConcurrentMap<String, SessionContext> sessions = new ConcurrentHashMap<>();

    @Override
    public SessionContext getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId, SessionContext::new);
    }
}
