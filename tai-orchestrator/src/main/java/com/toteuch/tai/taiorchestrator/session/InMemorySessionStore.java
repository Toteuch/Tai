package com.toteuch.tai.taiorchestrator.session;

import org.springframework.stereotype.Component;

@Component
public class InMemorySessionStore implements SessionStore {

    private SessionContext session;

    @Override
    public SessionContext get() {
        session = session == null ? new SessionContext() : session;
        return session;
    }
}
