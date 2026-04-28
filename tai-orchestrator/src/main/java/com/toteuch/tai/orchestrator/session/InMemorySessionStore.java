// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.session;

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
